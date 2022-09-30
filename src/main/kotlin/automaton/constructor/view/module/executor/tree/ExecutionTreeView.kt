package automaton.constructor.view.module.executor.tree

import automaton.constructor.controller.module.executor.tree.ExecutionTreeController
import automaton.constructor.model.module.executor.ExecutionState
import automaton.constructor.model.module.executor.Executor
import automaton.constructor.utils.customizedZoomScrollPane
import automaton.constructor.view.GRAPH_PANE_CENTER
import automaton.constructor.view.GRAPH_PANE_INIT_SIZE
import javafx.collections.SetChangeListener
import javafx.scene.layout.Pane
import tornadofx.*

class ExecutionTreeView(executor: Executor) : Pane() {
    val controller = ExecutionTreeController(executor)

    init {
        visibleWhen(executor.startedBinding)
        fun rerender() {
            clear()
            customizedZoomScrollPane {
                minWidth = GRAPH_PANE_INIT_SIZE.x
                minHeight = GRAPH_PANE_INIT_SIZE.y
                pane {
                    translateX = GRAPH_PANE_CENTER.x
                    val treeParent = this
                    val decorNode = pane()
                    hbox(ExecutionNodeView.HORIZONTAL_SPACING) {
                        translateXProperty().bind(layoutBoundsProperty().doubleBinding { -it!!.width / 2 })
                        val executionStateToNodeViewMap = mutableMapOf<ExecutionState, ExecutionNodeView>()
                        fun addRoot(root: ExecutionState) {
                            executionStateToNodeViewMap[root] =
                                executionNodeView(controller, root, treeParent, decorNode)
                        }
                        executor.roots.forEach { addRoot(it) }
                        executor.roots.addListener(SetChangeListener {
                            if (it.wasAdded()) addRoot(it.elementAdded)
                            if (it.wasRemoved()) executionStateToNodeViewMap.remove(it.elementRemoved)?.remove()
                        })
                    }
                }
            }.vvalue = 0.0
        }
        rerender()
        executor.startedBinding.onChange { rerender() }
    }
}
