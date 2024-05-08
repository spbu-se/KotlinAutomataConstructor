package automaton.constructor.view

import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.element.AutomatonVertex
import automaton.constructor.model.element.BuildingBlock
import automaton.constructor.model.element.Transition
import automaton.constructor.utils.hoverableTooltip
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.ListChangeListener
import javafx.scene.control.TableColumn
import javafx.scene.layout.Pane
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
        transitionsByVertices.forEach { map ->
            automaton.vertices.forEach { map.transitions[it] = SimpleObjectProperty(listOf()) }
        }
        sourceColumn.text = "Source"
        transitionsColumns.text = "Targets"
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
            registerColumn(newColumn)
        }
    }

    override fun unregisterVertex(vertex: AutomatonVertex) {
        transitionsByVertices.removeAll { it.source == vertex }
        unregisterColumn(
            transitionsColumns.columns.find { it.text == vertex.name } as TableColumn<TransitionMap<AutomatonVertex>, List<Transition>>)
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

    override fun registerColumn(addedColumn: TableColumn<TransitionMap<AutomatonVertex>, List<Transition>>) {
        val vertex = automaton.vertices.find { it.name == addedColumn.text }!!
        transitionsByVertices.forEach {
            it.transitions[vertex] = SimpleObjectProperty(listOf())
        }
        addedColumn.setCellValueFactory { p0 ->
            p0!!.value.transitions[vertex]!!
        }
        addedColumn.setCellFactory { TransitionsCell(this) }
        if (transitionsColumns.columns.none { it.text == addedColumn.text }) {
            transitionsColumns.columns.add(addedColumn)
        }
        transitionsColumns.columns.forEach { it.prefWidth = TRANSITIONS_COLUMNS_WIDTH / transitionsColumns.columns.size }
    }

    override fun unregisterColumn(removedColumn: TableColumn<TransitionMap<AutomatonVertex>, List<Transition>>) {
        transitionsColumns.columns.remove(removedColumn)
        transitionsColumns.columns.forEach { it.prefWidth = TRANSITIONS_COLUMNS_WIDTH / transitionsColumns.columns.size }
    }
}