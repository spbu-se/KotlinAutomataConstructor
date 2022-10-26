package automaton.constructor.view

import automaton.constructor.controller.AutomatonGraphController
import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.element.AutomatonVertex
import automaton.constructor.model.element.BuildingBlock
import automaton.constructor.model.element.State
import automaton.constructor.model.element.Transition
import automaton.constructor.utils.hoverableTooltip
import automaton.constructor.utils.subPane
import automaton.constructor.view.module.executor.executionStatesTooltip
import javafx.collections.SetChangeListener
import javafx.scene.layout.Pane
import tornadofx.*

class AutomatonGraphView(val automaton: Automaton, val automatonViewContext: AutomatonViewContext) : Pane() {
    private val edgePane = subPane()
    val edgeViews = mutableMapOf<Pair<AutomatonVertex, AutomatonVertex>, EdgeView>()
    val vertexToViewMap = mutableMapOf<AutomatonVertex, AutomatonVertexView>()
    val controller: AutomatonGraphController = AutomatonGraphController(automaton, automatonViewContext)

    init {
        minWidth = GRAPH_PANE_INIT_SIZE.x
        minHeight = GRAPH_PANE_INIT_SIZE.y
        controller.registerGraphView(this)
        automaton.vertices.forEach { registerVertex(it) }
        automaton.vertices.addListener(SetChangeListener {
            if (it.wasAdded()) registerVertex(it.elementAdded)
            if (it.wasRemoved()) unregisterVertex(it.elementRemoved)
        })
        automaton.transitions.forEach { registerTransition(it) }
        automaton.transitions.addListener(SetChangeListener {
            if (it.wasAdded()) registerTransition(it.elementAdded)
            if (it.wasRemoved()) unregisterTransition(it.elementRemoved)
        })
        controller.clearSelection()
    }

    private fun registerVertex(vertex: AutomatonVertex) {
        val automatonVertexView = AutomatonVertexView(vertex)
        controller.registerVertexView(automatonVertexView)
        vertexToViewMap[vertex] = automatonVertexView
        when (vertex) {
            is State -> automatonVertexView.hoverableTooltip { executionStatesTooltip(vertex) }
            is BuildingBlock -> {
                automatonVertexView.hoverableTooltip(stopManagingOnInteraction = true) {
                    Pane().apply {
                        minWidth = this@AutomatonGraphView.scene.window.width / 1.5
                        minHeight = this@AutomatonGraphView.scene.window.height / 1.5
                        maxWidth = this@AutomatonGraphView.scene.window.width / 1.5
                        maxHeight = this@AutomatonGraphView.scene.window.height / 1.5
                        val subAutomatonView = automatonViewContext.getAutomatonView(vertex.subAutomaton)
                        add(subAutomatonView)
                        subAutomatonView.fitToParentSize()
                    }
                }
            }
        }
        add(automatonVertexView)
    }

    private fun unregisterVertex(vertex: AutomatonVertex) {
        children.remove(vertexToViewMap.remove(vertex)!!)
    }

    private fun registerTransition(transition: Transition) {
        val edgeView = edgeViews.getOrPut(transition.source to transition.target) {
            EdgeView(
                vertexToViewMap.getValue(transition.source),
                vertexToViewMap.getValue(transition.target)
            ).also { newEdge ->
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
