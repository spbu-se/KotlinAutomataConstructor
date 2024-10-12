package automaton.constructor.view.automaton

import automaton.constructor.controller.AutomatonGraphController
import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.automaton.GRAPH_PANE_INIT_SIZE
import automaton.constructor.model.element.AutomatonEdge
import automaton.constructor.model.element.AutomatonVertex
import automaton.constructor.model.element.BuildingBlock
import automaton.constructor.model.element.State
import automaton.constructor.utils.hoverableTooltip
import automaton.constructor.utils.subPane
import automaton.constructor.view.AutomatonEdgeView
import automaton.constructor.view.elements.vertex.AutomatonVertexView
import automaton.constructor.view.AutomatonViewContext
import automaton.constructor.view.module.executor.executionStatesTooltip
import javafx.collections.MapChangeListener
import javafx.collections.SetChangeListener
import javafx.scene.layout.Pane
import tornadofx.add
import tornadofx.fitToParentSize
import kotlin.collections.set

class AutomatonGraphView(val automaton: Automaton, private val automatonViewContext: AutomatonViewContext) : AutomatonRepresentationView() {
    private val edgePane = subPane()
    val edgeViews = mutableMapOf<Pair<AutomatonVertex, AutomatonVertex>, AutomatonEdgeView>()
    val vertexToViewMap = mutableMapOf<AutomatonVertex, AutomatonVertexView>()
    override val controller: AutomatonGraphController = AutomatonGraphController(automaton, automatonViewContext)

    init {
        minWidth = GRAPH_PANE_INIT_SIZE.x
        minHeight = GRAPH_PANE_INIT_SIZE.y
        controller.registerGraphView(this)
        automaton.vertices.forEach { registerVertex(it) }
        automaton.vertices.addListener(SetChangeListener {
            if (it.wasAdded()) registerVertex(it.elementAdded)
            if (it.wasRemoved()) unregisterVertex(it.elementRemoved)
        })
        automaton.edges.values.forEach { registerEdge(it) }
        automaton.edges.addListener(MapChangeListener {
            if (it.wasAdded()) registerEdge(it.valueAdded)
            if (it.wasRemoved()) unregisterEdge(it.valueRemoved)
        })
        controller.clearSelection()
    }

    override fun getAllElementsViews() = edgeViews.values.flatMap { it.transitionViews } + vertexToViewMap.values

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
                        subAutomatonView.tablePrefHeight.bind(subAutomatonView.heightProperty())
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

    private fun registerEdge(edge: AutomatonEdge) {
        edgeViews[edge.source to edge.target] = AutomatonEdgeView(
            edge,
            vertexToViewMap.getValue(edge.source),
            vertexToViewMap.getValue(edge.target)
        ).also { edgeView ->
            edgeViews[edge.target to edge.source]?.let { oppositeEdgeView ->
                oppositeEdgeView.oppositeEdge = edgeView
                edgeView.oppositeEdge = oppositeEdgeView
            }
            edgePane.add(edgeView)
            controller.registerEdgeView(edgeView)
        }
    }

    private fun unregisterEdge(edge: AutomatonEdge) {
        val edgeView = edgeViews.getValue(edge.source to edge.target)
        edgePane.children.remove(edgeView)
        edgeViews.remove(edge.source to edge.target)
        edgeView.oppositeEdge?.oppositeEdge = null
    }

    fun transitionLayoutBounds() = edgeViews.values.flatMap { it.transitionViews }.associate {
        it.transition to it.layoutBounds
    }

    fun selectVertices(vertices: Set<AutomatonVertex>) =
        controller.select(vertices.mapNotNullTo(mutableSetOf()) { vertexToViewMap[it] })
}
