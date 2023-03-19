package automaton.constructor.controller

import automaton.constructor.model.action.Action
import automaton.constructor.model.action.ActionAvailability
import automaton.constructor.model.action.ActionFailedException
import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.automaton.allowsBuildingBlocks
import automaton.constructor.model.data.addContent
import automaton.constructor.model.element.*
import automaton.constructor.utils.*
import automaton.constructor.view.*
import javafx.geometry.Point2D
import javafx.scene.control.ContextMenu
import javafx.scene.input.KeyCode
import javafx.scene.input.MouseButton
import javafx.scene.shape.Line
import tornadofx.*

class AutomatonGraphController(val automaton: Automaton, val automatonViewContext: AutomatonViewContext) :
    Controller() {
    private val newTransitionLine = Line().apply { isVisible = false }
    private var newTransitionSourceProperty = objectProperty<AutomatonVertexView?>(null).apply {
        onChange {
            newTransitionLine.isVisible = it != null
            if (it != null) {
                newTransitionLine.startXProperty().unbind()
                newTransitionLine.startXProperty().bind(it.vertex.positionProperty.x)
                newTransitionLine.startYProperty().unbind()
                newTransitionLine.startYProperty().bind(it.vertex.positionProperty.y)
            }
        }
    }
    private var newTransitionSource by newTransitionSourceProperty
    val lastSelectedElementProperty = objectProperty<AutomatonElementView?>(null)
    var lastSelectedElement by lastSelectedElementProperty
    private val selectedElementsViews = mutableSetOf<AutomatonElementView>()

    fun registerGraphView(graphView: AutomatonGraphView) {
        graphView.add(newTransitionLine)
        graphView.setOnMouseClicked {
            it.consume()
            graphView.requestFocus()
            if (it.button == MouseButton.PRIMARY && it.isStillSincePress) clearSelection()
            else if (it.button == MouseButton.SECONDARY && it.isStillSincePress && automaton.allowsModificationsByUser) {
                ContextMenu().apply {
                    item(I18N.messages.getString("AutomatonGraphController.AddState")) {
                        action {
                            if (automaton.allowsModificationsByUser)
                                automaton.addState(position = Point2D(it.x, it.y))
                        }
                    }
                    if (automaton.allowsBuildingBlocks) {
                        item(I18N.messages.getString("AutomatonGraphController.AddEmptyBuildingBlock")) {
                            action {
                                if (automaton.allowsModificationsByUser)
                                    automaton.addBuildingBlock(position = Point2D(it.x, it.y))
                            }
                        }
                        item(I18N.messages.getString("AutomatonGraphController.CopyBuildingBlockFromFile")) {
                            action {
                                if (!automaton.allowsModificationsByUser) return@action
                                val file = automatonViewContext.fileController.chooseFile(
                                    I18N.messages.getString("MainView.File.Open"),
                                    FileChooserMode.Single
                                ) ?: return@action
                                automatonViewContext.fileController.loadAsync(file) addOnSuccess { (type, vertices, transitions, edges) ->
                                    if (type != automaton.getTypeData()) error(
                                        I18N.messages.getString("AutomatonGraphController.BuildingBlockLoadingFailed"),
                                        I18N.messages.getString("AutomatonGraphController.IncompatibleAutomatonType")
                                    )
                                    else {
                                        automaton.addBuildingBlock(position = Point2D(it.x, it.y)).apply {
                                            subAutomaton.addContent(vertices, transitions, edges)
                                            name = file.nameWithoutExtension
                                        }
                                    }
                                }
                            }
                        }
                    }
                    show(graphView.scene.window, it.screenX, it.screenY)
                }
            }
        }
        graphView.setOnMouseDragReleased {
            it.consume()
            newTransitionSource = null
        }
        graphView.focusedProperty().onChange { if (!it) newTransitionSource = null }
        graphView.setOnKeyPressed { event ->
            event.consume()
            if (event.code == KeyCode.DELETE && automaton.allowsModificationsByUser) {
                automaton.undoRedoManager.group {
                    selectedElementsViews.forEach {
                        when (it.automatonElement) {
                            is AutomatonVertex -> automaton.removeVertex(it.automatonElement)
                            is Transition -> automaton.removeTransition(it.automatonElement)
                        }
                    }
                }
                clearSelection()
            } else if (event.code == KeyCode.A && event.isControlDown) {
                clearSelection()
                selectedElementsViews.addAll(graphView.edgeViews.values.flatMap { it.transitionViews }
                    .onEach { it.selected = true })
                selectedElementsViews.addAll(graphView.vertexToViewMap.values.onEach { it.selected = true })
            }
        }
    }

    fun registerVertexView(automatonVertexView: AutomatonVertexView) {
        registerAutomatonElementView(automatonVertexView)
        if (automatonVertexView.vertex is BuildingBlock) automatonVertexView.onMouseClicked += {
            if (it.clickCount == 2) {
                it.consume()
                automatonViewContext.onBuildingBlockDoubleClicked(automatonVertexView.vertex)
            }
        }
        automatonVertexView.setOnMouseDragged {
            it.consume()
            if (it.button == MouseButton.PRIMARY && automatonVertexView.selected) {
                val change = Point2D(it.x, it.y) - automatonVertexView.vertex.position
                selectedElementsViews.forEach { elementView ->
                    if (elementView is AutomatonVertexView) {
                        elementView.vertex.position += change
                        elementView.vertex.requiresLayout = false
                        elementView.vertex.forceNoLayout = true
                    }
                }
            } else if (it.button == MouseButton.SECONDARY && automaton.allowsModificationsByUser) {
                automatonVertexView.vertex.requiresLayout = false
                newTransitionLine.endX = it.x
                newTransitionLine.endY = it.y
            }
        }
        automatonVertexView.setOnDragDetected {
            it.consume()
            if (it.button == MouseButton.PRIMARY) {
                if (!it.isControlDown && !automatonVertexView.selected) clearSelection()
                selectedElementsViews.add(automatonVertexView)
                automatonVertexView.selected = true
                lastSelectedElement = automatonVertexView
            } else if (it.button == MouseButton.SECONDARY) {
                newTransitionSource = automatonVertexView
                automatonVertexView.startFullDrag()
            }
        }
        automatonVertexView.setOnMouseReleased { event ->
            event.consume()
            if (!event.isStillSincePress) automaton.undoRedoManager.group {
                selectedElementsViews.forEach {
                    if (it is AutomatonVertexView) {
                        it.vertex.lastReleasePosition = it.vertex.position
                        it.vertex.forceNoLayout = false
                    }
                }
            }
        }
        automatonVertexView.setOnMouseDragReleased {
            it.consume()
            val source = newTransitionSourceProperty.value ?: return@setOnMouseDragReleased
            newTransitionSourceProperty.value = null
            if (automaton.allowsModificationsByUser) {
                source.vertex.requiresLayout = false
                automatonVertexView.vertex.requiresLayout = false
                automaton.addTransition(source.vertex, automatonVertexView.vertex)
            }
        }
    }

    fun registerEdgeView(edgeView: AutomatonEdgeView) {
        edgeView.setOnMouseClicked {
            it.consume()
            edgeView.requestFocus()
        }
        edgeView.transitionViews.forEach { registerTransitionView(it) }
        edgeView.transitionViews.onChange { change ->
            while (change.next())
                if (change.wasAdded())
                    change.addedSubList.forEach { registerTransitionView(it) }
        }
    }

    private fun registerTransitionView(transitionView: TransitionView) = registerAutomatonElementView(transitionView)

    private fun registerAutomatonElementView(automatonElementView: AutomatonElementView) {
        automatonElementView.setOnMouseClicked {
            it.consume()
            automatonElementView.requestFocus()
            if (it.button == MouseButton.PRIMARY) {
                if (it.isStillSincePress)
                    automaton.isOutputOfTransformation?.let { transformation ->
                        transformation.step(automatonElementView.automatonElement)
                        return@setOnMouseClicked
                    }
                lastSelectedElement = when {
                    !it.isControlDown -> {
                        clearSelection()
                        selectedElementsViews.add(automatonElementView)
                        automatonElementView.selected = true
                        automatonElementView
                    }
                    automatonElementView.selected -> {
                        selectedElementsViews.remove(automatonElementView)
                        automatonElementView.selected = false
                        null
                    }
                    else -> {
                        selectedElementsViews.add(automatonElementView)
                        automatonElementView.selected = true
                        automatonElementView
                    }
                }
            } else if (it.button == MouseButton.SECONDARY && it.isStillSincePress && automaton.allowsModificationsByUser) {
                fun <T : AutomatonElement> showActionsMenu(element: T, actions: List<Action<T>>) {
                    val actionsWithAvailability = actions.map { action ->
                        action to action.getAvailabilityFor(element)
                    }

                    if (actionsWithAvailability.any { (_, availability) -> availability != ActionAvailability.HIDDEN }) {
                        ContextMenu().apply {
                            for ((action, availability) in actionsWithAvailability) {
                                item(action.displayName, action.keyCombination) {
                                    action {
                                        try {
                                            if (automaton.allowsModificationsByUser)
                                                action.performOn(element)
                                        } catch (exc: ActionFailedException) {
                                            error(exc.message)
                                        }

                                    }
                                    isVisible = availability != ActionAvailability.HIDDEN
                                    isDisable = availability == ActionAvailability.DISABLED
                                }
                            }
                            show(automatonElementView.scene.window, it.screenX, it.screenY)
                        }
                        clearSelection()
                        selectedElementsViews.add(automatonElementView)
                        automatonElementView.selected = true
                        lastSelectedElement = automatonElementView
                    }
                }
                when (automatonElementView.automatonElement) {
                    is State -> showActionsMenu(
                        automatonElementView.automatonElement,
                        automaton.stateActions
                    )
                    is BuildingBlock -> showActionsMenu(
                        automatonElementView.automatonElement,
                        automaton.buildingBlockActions
                    )
                    is Transition -> showActionsMenu(
                        automatonElementView.automatonElement,
                        automaton.transitionActions
                    )
                }
            }
        }
        clearSelection()
        selectedElementsViews.add(automatonElementView)
        automatonElementView.selected = true
        lastSelectedElement = automatonElementView
    }

    fun clearSelection() {
        selectedElementsViews.onEach { it.selected = false }.clear()
        lastSelectedElement = null
    }
}
