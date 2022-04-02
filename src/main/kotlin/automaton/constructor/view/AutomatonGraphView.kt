package automaton.constructor.view

import automaton.constructor.controller.AutomatonGraphController
import automaton.constructor.model.Automaton
import automaton.constructor.model.State
import automaton.constructor.model.transition.Transition
import automaton.constructor.utils.subPane
import automaton.constructor.view.module.executor.installExecutionStateTooltip
import javafx.collections.SetChangeListener
import javafx.scene.layout.Pane
import tornadofx.*

class AutomatonGraphView(val automaton: Automaton) : Pane() {
    private val edgePane = subPane()
    val edgeViews = mutableMapOf<Pair<State, State>, EdgeView>()
    val stateToViewMap = mutableMapOf<State, StateView>()
    val controller: AutomatonGraphController = AutomatonGraphController(automaton)

    init {
        minWidth = GRAPH_PANE_INIT_SIZE.x
        minHeight = GRAPH_PANE_INIT_SIZE.y
        controller.registerGraphView(this)
        automaton.states.forEach { registerState(it) }
        automaton.states.addListener(SetChangeListener {
            if (it.wasAdded()) registerState(it.elementAdded)
            if (it.wasRemoved()) unregisterState(it.elementRemoved)
        })
        automaton.transitions.forEach { registerTransition(it) }
        automaton.transitions.addListener(SetChangeListener {
            if (it.wasAdded()) registerTransition(it.elementAdded)
            if (it.wasRemoved()) unregisterTransition(it.elementRemoved)
        })
        controller.clearSelection()
    }

    private fun registerState(state: State) {
        val stateView = StateView(state)
        controller.registerStateView(stateView)
        stateToViewMap[state] = stateView
        stateView.installExecutionStateTooltip()
        add(stateView.group)
    }

    private fun unregisterState(state: State) {
        children.remove(stateToViewMap.remove(state)!!.group)
    }

    private fun registerTransition(transition: Transition) {
        val edgeView = edgeViews.getOrPut(transition.source to transition.target) {
            EdgeView(transition.source, transition.target).also { newEdge ->
                edgeViews[transition.target to transition.source]?.let { oppositeEdge ->
                    oppositeEdge.oppositeEdge = newEdge
                    newEdge.oppositeEdge = oppositeEdge
                }
                edgePane.add(newEdge)
                newEdge.transitionViews.onChange {
                    if (newEdge.transitionViews.isEmpty()) {
                        edgePane.children.remove(newEdge)
                        edgeViews.remove(transition.source to transition.target)
                        newEdge.oppositeEdge?.oppositeEdge = null
                    }
                }
                controller.registerEdgeView(newEdge)
            }
        }
        edgeView.addTransition(transition)
    }

    private fun unregisterTransition(transition: Transition) {
        edgeViews.getValue(transition.source to transition.target).removeTransition(transition)
    }
}
