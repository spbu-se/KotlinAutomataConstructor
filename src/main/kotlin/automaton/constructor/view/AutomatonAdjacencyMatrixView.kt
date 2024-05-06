package automaton.constructor.view

import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.element.AutomatonVertex
import automaton.constructor.model.element.BuildingBlock
import automaton.constructor.model.element.Transition
import automaton.constructor.utils.hoverableTooltip
import javafx.beans.property.Property
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.scene.control.TableColumn
import javafx.scene.layout.Pane
import tornadofx.ChangeListener
import tornadofx.add
import tornadofx.fitToParentSize

class AutomatonAdjacencyMatrixView(automaton: Automaton, automatonViewContext: AutomatonViewContext
): AutomatonTableView<AdjacencyMatrixTransitionView>(automaton, automatonViewContext) {
    val verticesNamesListeners = mutableMapOf<Property<String>, ChangeListener<String>>()

    init {
        sourceColumn.text = "Source"
    }

    override fun registerVertex(vertex: AutomatonVertex) {
        val vertexView = AutomatonBasicVertexView(vertex)
        controller.registerAutomatonElementView(vertexView)
        if (vertex is BuildingBlock) {
            vertexView.hoverableTooltip(stopManagingOnInteraction = true) {
                Pane().apply {
                    minWidth = this@AutomatonAdjacencyMatrixView.scene.window.width / 1.5
                    minHeight = this@AutomatonAdjacencyMatrixView.scene.window.height / 1.5
                    maxWidth = this@AutomatonAdjacencyMatrixView.scene.window.width / 1.5
                    maxHeight = this@AutomatonAdjacencyMatrixView.scene.window.height / 1.5
                    val subAutomatonView = automatonViewContext.getAutomatonView(vertex.subAutomaton)
                    add(subAutomatonView)
                    subAutomatonView.fitToParentSize()
                }
            }
        }
        vertexToViewMap[vertex] = vertexView
        if (transitionsByVertices.none { it.source == vertex }) {
            transitionsByVertices.add(TransitionMap(vertex))
            transitionsColumns.add(TableColumn(vertex.name))
        }
        filtersCount[vertex.name] = 1
        verticesNamesListeners[vertex.nameProperty] = ChangeListener { observable, oldValue, newValue ->
            if (filtersCount.containsKey(newValue)) {

            }
        }
        vertex.nameProperty.addListener(ChangeListener())
    }

    override fun unregisterVertex(vertex: AutomatonVertex) {
        transitionsByVertices.removeAll { it.source == vertex }
        transitionsColumns.removeAll { it.text == vertex.name }
        vertexToViewMap.remove(vertex)
        filtersCount.remove(vertex.name)
    }

    override fun registerTransition(transition: Transition) {
        val transitionView = AdjacencyMatrixTransitionView(transition)
        controller.registerAutomatonElementView(transitionView)
        transitionToViewMap[transition] = transitionView
        transitionsByVertices.find { it.source == transition.source }.apply {
            val list = this!!.transitions[transition.target.name]!!.value
            this.transitions[transition.target.name]!!.set(list + transition)
        }
    }

    override fun unregisterTransition(transition: Transition) {
        transitionsByVertices.find { it.source == transition.source }.apply {
            val list = this!!.transitions[transition.target.name]!!.value
            this.transitions[transition.target.name]!!.set(list - transition)
        }
        transitionToViewMap.remove(transition)
    }

    private fun getVertexNameListener(): ChangeListener<String> = ChangeListener { observable, oldValue, newValue ->
        if (filtersCount.containsKey(newValue)) {

        }
    }
}