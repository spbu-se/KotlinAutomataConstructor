package automaton.constructor.view

import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.element.AutomatonVertex
import automaton.constructor.model.element.BuildingBlock
import automaton.constructor.model.element.Transition
import automaton.constructor.utils.hoverableTooltip
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.ListChangeListener
import javafx.scene.control.*
import javafx.scene.layout.Pane
import tornadofx.*

class AutomatonTransitionTableView(automaton: Automaton, automatonViewContext: AutomatonViewContext
): AutomatonTableView<TransitionTableTransitionView, String>(automaton, automatonViewContext) {
    private val filtersCount = mutableMapOf<String, Int>()
    init {
        transitionsByVertices.addListener(ListChangeListener {
            while (it.next()) {
                if (it.wasAdded()) {
                    val addedMap = it.addedSubList.first()
                    filtersCount.keys.forEach { filter ->
                        addedMap.transitions[filter] = SimpleObjectProperty(listOf())
                    }
                }
            }
        })
        transitionsByVertices.forEach { map ->
            filtersCount.keys.forEach { filter ->
                map.transitions[filter] = SimpleObjectProperty(listOf())
            }
        }
        sourceColumn.text = "State"
        transitionsColumns.text = "Inputs"
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
        if (transitionsByVertices.none { it.source == vertex }) {
            transitionsByVertices.add(TransitionMap(vertex))
        }
    }

    override fun unregisterVertex(vertex: AutomatonVertex) {
        transitionsByVertices.removeAll { it.source == vertex }
        vertexToViewMap.remove(vertex)
    }

    override fun registerTransition(transition: Transition) {
        val transitionView = TransitionTableTransitionView(transition)
        controller.registerAutomatonElementView(transitionView)
        transitionToViewMap[transition] = transitionView
        transition.filtersTextBinding.addListener { _, oldValue, _ ->
            deleteTransitionFromTable(transition, oldValue)
            addTransitionToTable(transition)
        }
        addTransitionToTable(transition)
    }

    override fun unregisterTransition(transition: Transition) {
        deleteTransitionFromTable(transition)
        transitionToViewMap.remove(transition)
    }
    
    private fun addTransitionToTable(transition: Transition) {
        if (!filtersCount.contains(transition.filtersText)) {
            registerColumn(TableColumn<TransitionMap<String>, List<Transition>>(transition.filtersText))
        }

        var transitionMap = transitionsByVertices.find { it.source == transition.source }
        if (transitionMap == null) {
            transitionMap = TransitionMap(transition.source)
            transitionsByVertices.add(transitionMap)
        }
        val list = transitionMap.transitions[transition.filtersText]!!.get()
        transitionMap.transitions[transition.filtersText]!!.set(list + transition)
        filtersCount[transition.filtersText] = filtersCount[transition.filtersText]!! + 1
    }
    
    private fun deleteTransitionFromTable(transition: Transition, filtersText: String = transition.filtersText) {
        transitionsByVertices.find { map ->
            map.source == transition.source
        }.apply {
            val list = this!!.transitions[filtersText]!!.value
            this.transitions[filtersText]!!.set(list - transition)
        }
        filtersCount[filtersText] = filtersCount[filtersText]!! - 1
        if (filtersCount[filtersText] == 0) {
            unregisterColumn(
                transitionsColumns.columns.find { it.text == filtersText } as TableColumn<TransitionMap<String>, List<Transition>>)
        }
    }

    override fun registerColumn(addedColumn: TableColumn<TransitionMap<String>, List<Transition>>) {
        filtersCount[addedColumn.text] = 0
        transitionsByVertices.forEach {
            it.transitions[addedColumn.text] = SimpleObjectProperty(listOf())
        }
        addedColumn.setCellValueFactory { p0 ->
            p0!!.value.transitions[addedColumn.text]!!
        }
        addedColumn.setCellFactory { TransitionsCell(this) }
        if (transitionsColumns.columns.none { it.text == addedColumn.text }) {
            transitionsColumns.columns.add(addedColumn)
        }
        transitionsColumns.columns.forEach { it.prefWidth = TRANSITIONS_COLUMNS_WIDTH / transitionsColumns.columns.size }
    }

    override fun unregisterColumn(removedColumn: TableColumn<TransitionMap<String>, List<Transition>>) {
        filtersCount.remove(removedColumn.text)
        transitionsColumns.columns.remove(removedColumn)
        transitionsColumns.columns.forEach { it.prefWidth = TRANSITIONS_COLUMNS_WIDTH / transitionsColumns.columns.size }
    }
}