package automaton.constructor.view.automaton

import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.element.AutomatonVertex
import automaton.constructor.model.element.Transition
import automaton.constructor.utils.I18N
import automaton.constructor.view.AdjacencyMatrixTransitionView
import automaton.constructor.view.AutomatonViewContext
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.ListChangeListener
import javafx.collections.SetChangeListener
import javafx.scene.control.TableColumn
import kotlin.math.max

class AdjacencyMatrixTransitionMap(
    val source: AutomatonVertex,
    val transitions: MutableMap<AutomatonVertex, SimpleObjectProperty<List<Transition>>> = mutableMapOf()
): TransitionMap

class AutomatonAdjacencyMatrixView(
    automaton: Automaton, automatonViewContext: AutomatonViewContext,
    tablePrefWidth: SimpleDoubleProperty, tablePrefHeight: SimpleDoubleProperty
): AutomatonTableView<AdjacencyMatrixTransitionView, AdjacencyMatrixTransitionMap>(
    automaton, automatonViewContext, tablePrefWidth, tablePrefHeight
) {
    private val transitionsColumns = TableColumn<AdjacencyMatrixTransitionMap, List<Transition>>(
        I18N.messages.getString("AutomatonAdjacencyMatrixView.Targets"))
    init {
        automaton.vertices.addListener(SetChangeListener {
            if (it.wasAdded()) {
                registerVertex(it.elementAdded)
            }
        })
        transitionsByVertices.addListener(ListChangeListener {
            while (it.next()) {
                if (it.wasAdded()) {
                    val addedMap = it.addedSubList.first()
                    automaton.vertices.forEach { addedMap.transitions[it] = SimpleObjectProperty(listOf()) }
                }
            }
        })
        transitionsByVertices.forEach { map ->
            automaton.vertices.forEach { map.transitions[it] = SimpleObjectProperty(listOf()) }
        }
        sourceColumn.text = I18N.messages.getString("AutomatonAdjacencyMatrixView.State")
        sourceColumn.minWidth = 200.0
        automaton.vertices.forEach { registerVertex(it) }
        automaton.transitions.forEach { registerTransition(it) }
        table.prefWidthProperty().addListener { _, _, _ ->
            resizeColumns()
        }
        table.columns.add(transitionsColumns)
    }

    private fun registerVertex(vertex: AutomatonVertex) {
        if (transitionsByVertices.none { it.source == vertex }) {
            transitionsByVertices.add(AdjacencyMatrixTransitionMap(vertex))
            val newColumn = TableColumn<AdjacencyMatrixTransitionMap, List<Transition>>(vertex.name)
            newColumn.textProperty().bind(vertex.nameProperty)
            registerColumn(newColumn)
        }
    }

    override fun unregisterVertex(vertex: AutomatonVertex) {
        transitionsByVertices.removeAll { it.source == vertex }
        unregisterColumn(
            transitionsColumns.columns.find { it.text == vertex.name } as TableColumn<AdjacencyMatrixTransitionMap, List<Transition>>)
    }

    override fun registerTransition(transition: Transition) {
        val transitionView = AdjacencyMatrixTransitionView(transition)
        controller.registerAutomatonElementView(transitionView)
        transitionToViewMap[transition] = transitionView
        transitionsByVertices.find { it.source == transition.source }.apply {
            val list = this!!.transitions[transition.target]!!.value
            this.transitions[transition.target]!!.set(list + transition)
        }
    }

    override fun unregisterTransition(transition: Transition) {
        transitionsByVertices.find { it.source == transition.source }.apply {
            val list = this!!.transitions[transition.target]!!.value
            this.transitions[transition.target]!!.set(list - transition)
        }
        transitionToViewMap.remove(transition)
    }

    private fun registerColumn(addedColumn: TableColumn<AdjacencyMatrixTransitionMap, List<Transition>>) {
        val vertex = automaton.vertices.find { it.name == addedColumn.text }!!
        transitionsByVertices.forEach {
            it.transitions[vertex] = SimpleObjectProperty(listOf())
        }
        addedColumn.setCellValueFactory { p0 ->
            p0!!.value.transitions[vertex]!!
        }
        addedColumn.setCellFactory { TransitionsCell(transitionToViewMap) }
        addedColumn.minWidth = 200.0
        if (transitionsColumns.columns.none { it.text == addedColumn.text }) {
            transitionsColumns.columns.add(addedColumn)
        }
        resizeColumns()
    }

    private fun unregisterColumn(removedColumn: TableColumn<AdjacencyMatrixTransitionMap, List<Transition>>) {
        transitionsColumns.columns.remove(removedColumn)
        resizeColumns()
    }

    private fun resizeColumns() {
        if (transitionsColumns.columns.isEmpty()) {
            table.columns.forEach { it.prefWidth = table.prefWidth / 2 }
        } else {
            sourceColumn.prefWidth = max(sourceColumn.minWidth, table.prefWidth / (transitionsColumns.columns.size + 1))
            transitionsColumns.columns.forEach {
                it.prefWidth = max(it.minWidth, table.prefWidth / (transitionsColumns.columns.size + 1))
            }
        }
    }
}
