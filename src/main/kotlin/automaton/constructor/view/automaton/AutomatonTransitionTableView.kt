package automaton.constructor.view.automaton

import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.element.AutomatonVertex
import automaton.constructor.model.element.Transition
import automaton.constructor.utils.I18N
import automaton.constructor.view.AutomatonViewContext
import automaton.constructor.view.TransitionTableTransitionView
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.*
import javafx.scene.control.cell.PropertyValueFactory

class TransitionTableTransitionMap(
    val source: AutomatonVertex,
    val target: AutomatonVertex,
    val transitions: SimpleObjectProperty<List<Transition>> = SimpleObjectProperty(listOf())
): TransitionMap

class AutomatonTransitionTableView(
    automaton: Automaton,
    automatonViewContext: AutomatonViewContext,
    tablePrefWidth: SimpleDoubleProperty,
    tablePrefHeight: SimpleDoubleProperty
): AutomatonTableView<TransitionTableTransitionView, TransitionTableTransitionMap>(
    automaton, automatonViewContext, tablePrefWidth, tablePrefHeight) {
    private val targetColumn = TableColumn<TransitionTableTransitionMap, AutomatonVertex>(
        I18N.messages.getString("AutomatonTransitionTableView.ToState"))
    private val transitionColumn = TableColumn<TransitionTableTransitionMap, List<Transition>>(
        I18N.messages.getString("AutomatonTransitionTableView.Label"))

    init {
        sourceColumn.text = I18N.messages.getString("AutomatonTransitionTableView.FromState")
        targetColumn.cellValueFactory = PropertyValueFactory("target")
        targetColumn.setCellFactory { VertexCell(this) }
        transitionColumn.setCellValueFactory { p0 ->
            p0!!.value.transitions
        }
        transitionColumn.setCellFactory { TransitionsCell(transitionToViewMap) }
        table.columns.addAll(targetColumn, transitionColumn)
        table.prefWidthProperty().addListener { _, _, newValue ->
            table.columns.forEach { it.prefWidth = (newValue as Double) / 3 }
        }
        automaton.transitions.forEach { registerTransition(it) }
    }

    override fun unregisterVertex(vertex: AutomatonVertex) {
        transitionsByVertices.removeAll { it.source == vertex }
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
