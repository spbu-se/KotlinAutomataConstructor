package automaton.constructor.controller

import automaton.constructor.model.automaton.Automaton
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
    private var newTransitionSourceProperty = objectProperty<StateView?>(null).apply {
        onChange {
            newTransitionLine.isVisible = it != null
            if (it != null) {
                newTransitionLine.startXProperty().unbind()
                newTransitionLine.startXProperty().bind(it.state.positionProperty.x)
                newTransitionLine.startYProperty().unbind()
                newTransitionLine.startYProperty().bind(it.state.positionProperty.y)
            }
        }
    }
    private var newTransitionSource by newTransitionSourceProperty
    val lastSelectedElementProperty = objectProperty<AutomatonElementView?>(null)
    var lastSelectedElement by lastSelectedElementProperty
    private val selectedStateViews = mutableSetOf<StateView>()
    private val selectedTransitionViews = mutableSetOf<TransitionView>()

    fun registerGraphView(graphView: AutomatonGraphView) {
        graphView.add(newTransitionLine)
        graphView.setOnMouseClicked {
            it.consume()
            graphView.requestFocus()
            if (it.button == MouseButton.PRIMARY && it.isStillSincePress) clearSelection()
            else if (it.button == MouseButton.SECONDARY && it.isStillSincePress) {
                ContextMenu().apply {
                    item("Add state") {
                        action {
                            automaton.addState(position = Point2D(it.x, it.y))
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
                    selectedTransitionViews.forEach { transitionView -> automaton.removeTransition(transitionView.transition) }
                    selectedStateViews.forEach { stateView -> automaton.removeState(stateView.state) }
                }
                clearSelection()
            } else if (event.code == KeyCode.A && event.isControlDown) {
                clearSelection()
                selectedTransitionViews.addAll(graphView.edgeViews.values.flatMap { it.transitionViews }
                    .onEach { it.selected = true })
                selectedStateViews.addAll(graphView.stateToViewMap.values.onEach { it.selected = true })
            }
        }
    }

    fun registerStateView(stateView: StateView) {
        stateView.group.setOnMouseClicked {
            it.consume()
            stateView.group.requestFocus()
            if (it.button == MouseButton.PRIMARY && it.isStillSincePress) {
                lastSelectedElement = when {
                    !it.isControlDown -> {
                        clearSelection()
                        selectedStateViews.add(stateView)
                        stateView.selected = true
                        stateView
                    }
                    stateView.selected -> {
                        selectedStateViews.remove(stateView)
                        stateView.selected = false
                        null
                    }
                    else -> {
                        selectedStateViews.add(stateView)
                        stateView.selected = true
                        stateView
                    }
                }
            } else if (it.button == MouseButton.SECONDARY && it.isStillSincePress) {
                val actions = automaton.stateActions.filter { action ->
                    action.isAvailableFor(stateView.state)
                }
                if (actions.isNotEmpty()) {
                    ContextMenu().apply {
                        for (action in actions) {
                            item(action.displayName) {
                                action { action.performOn(stateView.state) }
                            }
                        }
                        show(stateView.group.scene.window, it.screenX, it.screenY)
                    }
                }
            }
        }
        stateView.group.setOnMouseDragged {
            it.consume()
            if (it.button == MouseButton.PRIMARY && stateView.selected) {
                val change = Point2D(it.x, it.y) - stateView.state.position
                selectedStateViews.forEach { curStateView -> curStateView.state.position += change }
            } else if (it.button == MouseButton.SECONDARY) {
                newTransitionLine.endX = it.x
                newTransitionLine.endY = it.y
            }
        }
        stateView.group.setOnDragDetected {
            it.consume()
            if (it.button == MouseButton.PRIMARY) {
                if (!it.isControlDown && !stateView.selected) clearSelection()
                selectedStateViews.add(stateView)
                stateView.selected = true
                lastSelectedElement = stateView
            } else if (it.button == MouseButton.SECONDARY) {
                newTransitionSource = stateView
                stateView.group.startFullDrag()
            }
        }
        stateView.group.setOnMouseReleased { event ->
            event.consume()
            if (!event.isStillSincePress) automaton.undoRedoManager.group {
                selectedStateViews.forEach { it.state.lastReleasePosition = it.state.position }
            }
        }
        stateView.group.setOnMouseDragReleased {
            it.consume()
            val source = newTransitionSourceProperty.value ?: return@setOnMouseDragReleased
            newTransitionSourceProperty.value = null
            automaton.addTransition(source.state, stateView.state)
        }
        clearSelection()
        selectedStateViews.add(stateView)
        stateView.selected = true
        lastSelectedElement = stateView
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

    private fun registerTransitionView(transitionView: TransitionView) {
        transitionView.text.setOnMouseClicked {
            it.consume()
            transitionView.text.requestFocus()
            if (it.button == MouseButton.PRIMARY) {
                lastSelectedElement = when {
                    !it.isControlDown -> {
                        clearSelection()
                        selectedTransitionViews.add(transitionView)
                        transitionView.selected = true
                        transitionView
                    }
                    transitionView.selected -> {
                        selectedTransitionViews.remove(transitionView)
                        transitionView.selected = false
                        null
                    }
                    else -> {
                        selectedTransitionViews.add(transitionView)
                        transitionView.selected = true
                        transitionView
                    }
                }
            } else if (it.button == MouseButton.SECONDARY && it.isStillSincePress) {
                val actions = automaton.transitionActions.filter { action ->
                    action.isAvailableFor(transitionView.transition)
                }
                if (actions.isNotEmpty()) {
                    ContextMenu().apply {
                        for (action in actions) {
                            item(action.displayName) {
                                action { action.performOn(transitionView.transition) }
                            }
                        }
                        show(transitionView.text.scene.window, it.screenX, it.screenY)
                    }
                }
            }
        }
        clearSelection()
        selectedTransitionViews.add(transitionView)
        transitionView.selected = true
        lastSelectedElement = transitionView
    }

    fun clearSelection() {
        selectedStateViews.onEach { it.selected = false }.clear()
        selectedTransitionViews.onEach { it.selected = false }.clear()
        lastSelectedElement = null
    }
}
