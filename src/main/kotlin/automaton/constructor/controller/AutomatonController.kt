package automaton.constructor.controller

import automaton.constructor.model.Automaton
import automaton.constructor.model.State
import automaton.constructor.utils.SettingsHolder
import automaton.constructor.utils.x
import automaton.constructor.utils.y
import automaton.constructor.view.AutomatonGraphView
import automaton.constructor.view.EdgeView
import automaton.constructor.view.StateView
import automaton.constructor.view.TransitionView
import javafx.geometry.Point2D
import javafx.scene.control.ContextMenu
import javafx.scene.input.KeyCode
import javafx.scene.input.MouseButton
import javafx.scene.shape.Line
import tornadofx.*

class AutomatonController(val automaton: Automaton) : Controller() {
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
    val selectedSettingsHolderProperty = objectProperty<SettingsHolder?>(null)
    var selectedSettingsHolder by selectedSettingsHolderProperty
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
                            automaton.addState(State(position = Point2D(it.x, it.y)))
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
                selectedTransitionViews.forEach { transitionView -> automaton.removeTransition(transitionView.transition) }
                selectedStateViews.forEach { stateView -> automaton.removeState(stateView.state) }
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
        stateView.setOnMouseClicked {
            it.consume()
            stateView.requestFocus()
            if (it.button == MouseButton.PRIMARY && it.isStillSincePress)
                selectedSettingsHolder = when {
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
        }
        stateView.setOnMouseDragged {
            it.consume()
            if (it.button == MouseButton.PRIMARY && stateView.selected) {
                val change = Point2D(it.x, it.y) - stateView.state.position
                selectedStateViews.forEach { curStateView -> curStateView.state.position += change }
            } else if (it.button == MouseButton.SECONDARY) {
                newTransitionLine.endX = it.x
                newTransitionLine.endY = it.y
            }
        }
        stateView.setOnDragDetected {
            it.consume()
            if (it.button == MouseButton.PRIMARY) {
                if (!it.isControlDown && !stateView.selected) clearSelection()
                selectedStateViews.add(stateView)
                stateView.selected = true
                selectedSettingsHolder = stateView
            } else if (it.button == MouseButton.SECONDARY) {
                newTransitionSource = stateView
                stateView.startFullDrag()
            }
        }
        stateView.setOnMouseDragReleased {
            it.consume()
            val source = newTransitionSourceProperty.value ?: return@setOnMouseDragReleased
            newTransitionSourceProperty.value = null
            automaton.addTransition(source.state, stateView.state)
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

    private fun registerTransitionView(transitionView: TransitionView) {
        transitionView.setOnMouseClicked {
            it.consume()
            transitionView.requestFocus()
            if (it.button == MouseButton.PRIMARY)
                selectedSettingsHolder = when {
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
        }
    }

    private fun clearSelection() {
        selectedStateViews.onEach { it.selected = false }.clear()
        selectedTransitionViews.onEach { it.selected = false }.clear()
        selectedSettingsHolder = null
    }
}
