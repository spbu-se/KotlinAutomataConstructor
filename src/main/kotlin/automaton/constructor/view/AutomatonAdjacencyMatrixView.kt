package automaton.constructor.view

import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.element.AutomatonVertex
import automaton.constructor.model.element.BuildingBlock
import automaton.constructor.model.element.Transition
import automaton.constructor.utils.hoverableTooltip
import javafx.beans.property.Property
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.ListChangeListener
import javafx.scene.control.TableColumn
import javafx.scene.layout.Pane
import tornadofx.ChangeListener
import tornadofx.add
import tornadofx.fitToParentSize

class AutomatonAdjacencyMatrixView(automaton: Automaton, automatonViewContext: AutomatonViewContext
): AutomatonTableView<AdjacencyMatrixTransitionView, AutomatonVertex>(automaton, automatonViewContext) {
    init {
        transitionsByVertices.addListener(ListChangeListener {
            while (it.next()) {
                if (it.wasAdded()) {
                    val addedMap = it.addedSubList.first()
                    automaton.vertices.forEach { addedMap.transitions[it] = SimpleObjectProperty(listOf()) }
                }
            }
        })
        transitionsColumns.addListener(ListChangeListener {
            while (it.next()) {
                if (it.wasAdded()) {
                    registerColumn(it.addedSubList.first())
                }
                if (it.wasRemoved()) {
                    val removedColumn = it.removed.first()
                    table.columns.remove(removedColumn)
                }
            }
        })
        transitionsByVertices.forEach { map ->
            automaton.vertices.forEach { map.transitions[it] = SimpleObjectProperty(listOf()) }
        }
        transitionsColumns.forEach { registerColumn(it) }
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
            val newColumn = TableColumn<TransitionMap<AutomatonVertex>, List<Transition>>(vertex.name)
            newColumn.textProperty().bind(vertex.nameProperty)
            transitionsColumns.add(newColumn)
        }
    }

    override fun unregisterVertex(vertex: AutomatonVertex) {
        transitionsByVertices.removeAll { it.source == vertex }
        transitionsColumns.removeAll { it.text == vertex.name }
        vertexToViewMap.remove(vertex)
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

    fun registerColumn(addedColumn: TableColumn<TransitionMap<AutomatonVertex>, List<Transition>>) {
        val vertex = automaton.vertices.find { it.name == addedColumn.text }!!
        transitionsByVertices.forEach {
            it.transitions[vertex] = SimpleObjectProperty(listOf())
        }
        addedColumn.setCellValueFactory { p0 ->
            p0!!.value.transitions[vertex]!!
        }
        addedColumn.setCellFactory { TransitionsCell(this) }
        addedColumn.minWidth = computeCellWidth(addedColumn.text.length)
        table.columns.add(addedColumn)
    }
}