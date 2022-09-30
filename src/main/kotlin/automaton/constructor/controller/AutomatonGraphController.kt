package automaton.constructor.controller

import automaton.constructor.model.action.ActionAvailability
import automaton.constructor.model.action.AutomatonElementAction
import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.automaton.allowsBuildingBlocks
import automaton.constructor.model.element.*
import automaton.constructor.utils.I18N
import automaton.constructor.utils.x
import automaton.constructor.utils.y
import automaton.constructor.view.*
import javafx.geometry.Point2D
import javafx.scene.control.ContextMenu
import javafx.scene.input.KeyCode
import javafx.scene.input.MouseButton
import javafx.scene.shape.Line
import tornadofx.*

class AutomatonGraphController(val automaton: Automaton) : Controller() {
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
            else if (it.button == MouseButton.SECONDARY && it.isStillSincePress) {
                ContextMenu().apply {
                    item(I18N.messages.getString("AutomatonGraphController.AddState")) {
                        action {
                            automaton.addState(position = Point2D(it.x, it.y))
                        }
                    }
                    if (automaton.allowsBuildingBlocks)
                        item(I18N.messages.getString("AutomatonGraphController.AddBuildingBlock")) {
                            action {
                                automaton.addBuildingBlock(position = Point2D(it.x, it.y))
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
            if (event.code == KeyCode.DELETE) {
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
        automatonVertexView.setOnMouseDragged {
            it.consume()
            if (it.button == MouseButton.PRIMARY && automatonVertexView.selected) {
                val change = Point2D(it.x, it.y) - automatonVertexView.vertex.position
                selectedElementsViews.forEach { elementView ->
                    if (elementView is AutomatonVertexView)
                        elementView.vertex.position += change
                }
            } else if (it.button == MouseButton.SECONDARY) {
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
                    if (it is AutomatonVertexView)
                        it.vertex.lastReleasePosition = it.vertex.position
                }
            }
        }
        automatonVertexView.setOnMouseDragReleased {
            it.consume()
            val source = newTransitionSourceProperty.value ?: return@setOnMouseDragReleased
            newTransitionSourceProperty.value = null
            automaton.addTransition(source.vertex, automatonVertexView.vertex)
        }
    }

    fun registerEdgeView(edgeView: EdgeView) {
        edgeView.setOnMouseClicked {
            it.consume()
            edgeView.requestFocus()
        }
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
            } else if (it.button == MouseButton.SECONDARY && it.isStillSincePress) {
                fun <T : AutomatonElement> showActionsMenu(element: T, actions: List<AutomatonElementAction<T>>) {
                    val actionsWithAvailability = actions.map { action ->
                        action to action.getAvailabilityFor(element)
                    }

                    if (actionsWithAvailability.any { (_, availability) -> availability != ActionAvailability.HIDDEN }) {
                        ContextMenu().apply {
                            for ((action, availability) in actionsWithAvailability) {
                                item(action.displayName, action.keyCombination) {
                                    action { action.performOn(element) }
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
