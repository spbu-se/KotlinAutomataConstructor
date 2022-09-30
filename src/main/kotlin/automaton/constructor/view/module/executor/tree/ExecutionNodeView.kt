package automaton.constructor.view.module.executor.tree

import automaton.constructor.controller.module.executor.tree.ExecutionTreeController
import automaton.constructor.model.module.executor.ExecutionState
import automaton.constructor.model.module.executor.SimpleExecutionState
import automaton.constructor.model.module.executor.SuperExecutionState
import automaton.constructor.utils.*
import automaton.constructor.view.AutomatonVertexView
import automaton.constructor.view.EdgeView
import automaton.constructor.view.TransitionLabelPosition
import automaton.constructor.view.module.executor.color
import automaton.constructor.view.module.executor.simpleTooltipContent
import javafx.beans.value.ObservableValue
import javafx.collections.SetChangeListener
import javafx.event.EventTarget
import javafx.geometry.Point2D
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.input.MouseButton
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import tornadofx.*

fun EventTarget.executionNodeView(
    controller: ExecutionTreeController,
    executionState: ExecutionState,
    treeParent: Parent,
    decorNode: Node,
    op: ExecutionNodeView.() -> Unit = {}
) = ExecutionNodeView(controller, executionState, treeParent, decorNode).also {
    add(it)
    it.init()
    it.op()
}

class ExecutionNodeView(
    val controller: ExecutionTreeController,
    val executionState: ExecutionState,
    /**
     * Used to translate the entire execution tree whenever this node is expanded or collapsed
     * so that this node remains unmoved on the screen
     */
    val treeParent: Parent,
    /**
     * Used as a parent for nodes that should not affect tree layout (e.g. transitions and init markers)
     */
    val decorNode: Node
) : VBox(VERTICAL_SPACING) {

    companion object {
        const val VERTICAL_SPACING = 300.0
        const val HORIZONTAL_SPACING = 60.0
    }

    private val childToViewMap = mutableMapOf<ExecutionState, Pair<ExecutionNodeView, EdgeView>>()
    private lateinit var observableCenterInDecorNode: ObservableValue<Point2D>
    private lateinit var automatonVertexView: AutomatonVertexView
    private lateinit var childrenBox: HBox

    fun init() {
        alignment = Pos.BASELINE_CENTER
        automatonVertexView = AutomatonVertexView(executionState.vertex)
        automatonVertexView.positionProperty.bind(Point2D.ZERO.toProperty())
        automatonVertexView.colorProperty.bind(executionState.statusProperty.nonNullObjectBinding {
            it.color ?: AutomatonVertexView.DEFAULT_COLOR
        })
        automatonVertexView.initMarker.removeFromParent()
        add(automatonVertexView)
        observableCenterInDecorNode = automatonVertexView.localToAncestor(decorNode.parent, Point2D.ZERO.toProperty())
        automatonVertexView.initMarker.translateXProperty().bind(observableCenterInDecorNode.x)
        automatonVertexView.initMarker.translateYProperty().bind(observableCenterInDecorNode.y)
        decorNode.add(automatonVertexView.initMarker)

        when (executionState) {
            is SimpleExecutionState -> automatonVertexView.hoverableTooltip { executionState.simpleTooltipContent() }
            is SuperExecutionState -> {
                val executionTreeView by lazy { ExecutionTreeView(executionState.subExecutor) } // memoization
                automatonVertexView.hoverableTooltip {
                    if (executionState.subExecutor.started) Pane().apply {
                        minWidth = FX.primaryStage.width / 4.0
                        minHeight = FX.primaryStage.height / 1.5
                        maxWidth = FX.primaryStage.width / 4.0
                        maxHeight = FX.primaryStage.height / 1.5
                        add(executionTreeView)
                        executionTreeView.fitToParentSize()
                    } else executionState.simpleTooltipContent()
                }
            }
        }
        automatonVertexView.setOnMouseClicked { event ->
            if (event.button == MouseButton.PRIMARY && event.isStillSincePress) {
                event.consume()
                fun posInTreeParent() = treeParent.sceneToLocal(automatonVertexView.localToScene(Point2D.ZERO))
                val posInTreeParentBefore = posInTreeParent()
                controller.toggleExecutionState(executionState)
                treeParent.layout()
                val posInTreeParentDelta = posInTreeParent() - posInTreeParentBefore
                treeParent.translateX -= posInTreeParentDelta.x
                treeParent.translateY -= posInTreeParentDelta.y
            }
        }
        childrenBox = hbox(HORIZONTAL_SPACING)
        executionState.children.forEach { onChildAdded(it) }
        executionState.children.addListener(SetChangeListener { change ->
            if (change.wasAdded()) onChildAdded(change.elementAdded)
            if (change.wasRemoved()) onChildRemoved(change.elementRemoved)
        })
    }

    fun remove() {
        removeFromParent()
        removeDecor()
    }

    private fun onChildAdded(child: ExecutionState) {
        val nodeView = childrenBox.executionNodeView(controller, child, treeParent, decorNode)
        val edgeView = EdgeView(
            automatonVertexView,
            nodeView.automatonVertexView,
            observableCenterInDecorNode,
            nodeView.observableCenterInDecorNode,
            TransitionLabelPosition.ABOVE
        )
        child.lastTransition?.let { edgeView.addTransition(it) }
        decorNode.add(edgeView)
        childToViewMap[child] = nodeView to edgeView
    }

    private fun onChildRemoved(child: ExecutionState) {
        val (nodeView, edgeView) = childToViewMap.remove(child)!!
        nodeView.remove()
        edgeView.removeFromParent()
    }

    private fun removeDecor() {
        automatonVertexView.initMarker.removeFromParent()
        childToViewMap.values.forEach { (nodeView, edgeView) ->
            nodeView.removeDecor()
            edgeView.removeFromParent()
        }
    }
}
