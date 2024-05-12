package automaton.constructor.view

import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.element.AutomatonVertex
import automaton.constructor.model.element.BuildingBlock
import automaton.constructor.model.element.Transition
import automaton.constructor.utils.hoverableTooltip
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.*
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.layout.Pane
import tornadofx.*

class TransitionTableTransitionMap(
    val source: AutomatonVertex,
    val target: AutomatonVertex,
    val transitions: SimpleObjectProperty<List<Transition>> = SimpleObjectProperty(listOf())
): TransitionMap

class AutomatonTransitionTableView(automaton: Automaton, automatonViewContext: AutomatonViewContext
): AutomatonTableView<TransitionTableTransitionView, TransitionTableTransitionMap>(automaton, automatonViewContext) {
    private val targetColumn = TableColumn<TransitionTableTransitionMap, AutomatonVertex>("To state")
    private val transitionColumn = TableColumn<TransitionTableTransitionMap, List<Transition>>("Label")

    init {
        sourceColumn.text = "From state"
        targetColumn.cellValueFactory = PropertyValueFactory("target")
        targetColumn.setCellFactory { VertexCell(this) }
        transitionColumn.setCellValueFactory { p0 ->
            p0!!.value.transitions
        }
        transitionColumn.setCellFactory { TransitionsCell(this) }
        table.columns.addAll(targetColumn, transitionColumn)
        automaton.vertices.forEach { registerVertex(it) }
        automaton.transitions.forEach { registerTransition(it) }
    }

    override fun registerVertex(vertex: AutomatonVertex) {
        val vertexView = AutomatonBasicVertexView(vertex)
        controller.registerAutomatonElementView(vertexView)
        if (vertex is BuildingBlock) {
            vertexView.hoverableTooltip(stopManagingOnInteraction = true) {
                Pane().apply {
                    minWidth = this@AutomatonTransitionTableView.scene.window.width / 1.5
                    minHeight = this@AutomatonTransitionTableView.scene.window.height / 1.5
                    maxWidth = this@AutomatonTransitionTableView.scene.window.width / 1.5
                    maxHeight = this@AutomatonTransitionTableView.scene.window.height / 1.5
                    val subAutomatonView = automatonViewContext.getAutomatonView(vertex.subAutomaton)
                    add(subAutomatonView)
                    subAutomatonView.fitToParentSize()
                }
            }
        }
        vertexToViewMap[vertex] = vertexView
    }

    override fun unregisterVertex(vertex: AutomatonVertex) {
        transitionsByVertices.removeAll { it.source == vertex }
        vertexToViewMap.remove(vertex)
    }

    override fun registerTransition(transition: Transition) {
        val transitionView = TransitionTableTransitionView(transition)
        controller.registerAutomatonElementView(transitionView)
        transitionToViewMap[transition] = transitionView
        addTransitionToTable(transition)
    }

    override fun unregisterTransition(transition: Transition) {
        deleteTransitionFromTable(transition)
        transitionToViewMap.remove(transition)
    }
    
    private fun addTransitionToTable(transition: Transition) {
        var transitionMap = transitionsByVertices.find {
            it.source == transition.source && it.target == transition.target
        }
        if (transitionMap == null) {
            transitionMap = TransitionTableTransitionMap(transition.source, transition.target)
            transitionsByVertices.add(transitionMap)
        }
        val list = transitionMap.transitions.get()
        transitionMap.transitions.set(list + transition)
    }
    
    private fun deleteTransitionFromTable(transition: Transition) {
        transitionsByVertices.find { map ->
            map.source == transition.source && map.target == transition.target
        }.also {
            val list = it!!.transitions.value
            it.transitions.set(list - transition)
            if (it.transitions.value.isEmpty()) {
                transitionsByVertices.remove(it)
            }
        }
    }
}