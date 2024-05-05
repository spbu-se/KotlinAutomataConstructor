package automaton.constructor.view

import automaton.constructor.controller.AutomatonRepresentationController
import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.element.AutomatonVertex
import automaton.constructor.model.element.Transition
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ListChangeListener
import javafx.collections.SetChangeListener
import javafx.scene.control.TableCell
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import tornadofx.*

class SourceCell(private val matrix: AutomatonAdjacencyMatrixView): TableCell<TransitionMap, AutomatonVertex>() {
    private val colourProperty = SimpleStringProperty("")
    private var colour by colourProperty
    override fun updateItem(item: AutomatonVertex?, empty: Boolean) {
        super.updateItem(item, empty)
        if (item != null) {
            colourProperty.bind(matrix.vertexToViewMap[item]!!.colourProperty)
            this.style = "-fx-background-color: ${colour};"
            colourProperty.addListener(ChangeListener { _, _, newValue ->
                this.style = "-fx-background-color: ${newValue};"
            })
            graphic = matrix.vertexToViewMap[item]
        } else {
            this.style = "-fx-background-color: white;"
            graphic = null
        }
    }
}

class TargetCell(private val matrix: AutomatonAdjacencyMatrixView): TableCell<TransitionMap, List<Transition>>() {
    override fun updateItem(item: List<Transition>?, empty: Boolean) {
        super.updateItem(item, empty)
        graphic = if (item != null) {
            VBox().apply {
                item.forEach {
                    add(matrix.transitionToViewMap[it]!!)
                    val prefWidth = matrix.computeCellWidth(matrix.transitionToViewMap[it]!!.textLength)
                    this@TargetCell.tableColumn.prefWidth = maxOf(this@TargetCell.tableColumn.prefWidth, prefWidth)
                }
            }
        } else {
            null
        }
    }
}

class AutomatonAdjacencyMatrixView(
    private val automaton: Automaton,
    automatonViewContext: AutomatonViewContext
): Pane() {
    private val transitionsByVertices = observableListOf<TransitionMap>()
    private val adjacencyMatrix = TableView(transitionsByVertices)
    private val sourceColumn = TableColumn<TransitionMap, AutomatonVertex>("Source")
    private val targetColumns = observableListOf<TableColumn<TransitionMap, List<Transition>>>()
    val controller = AutomatonRepresentationController(automaton, automatonViewContext)
    val vertexToViewMap = mutableMapOf<AutomatonVertex, AutomatonBasicVertexView>()
    val transitionToViewMap = mutableMapOf<Transition, AdjacencyMatrixTransitionView>()

    init {
        automaton.vertices.addListener(SetChangeListener {
            if (it.wasAdded()) {
                registerVertex(it.elementAdded)
            }
            if (it.wasRemoved()) {
                unregisterVertex(it.elementRemoved)
            }
        })
        automaton.transitions.addListener(SetChangeListener {
            if (it.wasAdded()) {
                registerTransition(it.elementAdded)
            }
            if (it.wasRemoved()) {
                unregisterTransition(it.elementRemoved)
            }
        })
        transitionsByVertices.addListener(ListChangeListener {
            while (it.next()) {
                if (it.wasAdded()) {
                    val addedMap = it.addedSubList.first()
                    automaton.vertices.forEach {
                        addedMap.transitions[it.name] = SimpleObjectProperty(listOf())
                    }
                }
            }
        })
        targetColumns.addListener(ListChangeListener {
            while (it.next()) {
                if (it.wasAdded()) {
                    val addedColumn = it.addedSubList.first()
                    transitionsByVertices.forEach {
                        it.transitions[addedColumn.text] = SimpleObjectProperty(listOf())
                    }
                    addedColumn.setCellValueFactory { p0 ->
                        p0!!.value.transitions[addedColumn.text]!!
                    }
                    addedColumn.setCellFactory { TargetCell(this) }
                    addedColumn.minWidth = computeCellWidth(addedColumn.text.length)
                    adjacencyMatrix.columns.add(addedColumn)
                }
                if (it.wasRemoved()) {
                    val removedColumn = it.removed.first()
                    adjacencyMatrix.columns.remove(removedColumn)
                }
            }
        })

        vbox {
            add(adjacencyMatrix)
        }

        automaton.vertices.forEach { registerVertex(it) }
        automaton.transitions.forEach { registerTransition(it) }
        sourceColumn.cellValueFactory = PropertyValueFactory("source")
        sourceColumn.setCellFactory { SourceCell(this) }
        sourceColumn.minWidth = 150.0
        adjacencyMatrix.columns.add(0, sourceColumn)

        adjacencyMatrix.style {
            minWidth = 1900.0.px
            minHeight = 1000.0.px
            fontSize = 40.0.px
        }
    }

    private fun registerVertex(vertex: AutomatonVertex) {
        val vertexView = AutomatonBasicVertexView(vertex)
        controller.registerAutomatonElementView(vertexView)
        vertexToViewMap[vertex] = vertexView
        if (transitionsByVertices.none { it.source == vertex }) {
            transitionsByVertices.add(TransitionMap(vertex))
            targetColumns.add(TableColumn(vertex.name))
        }
    }

    private fun unregisterVertex(vertex: AutomatonVertex) {
        transitionsByVertices.removeAll { it.source == vertex }
        targetColumns.removeAll { it.text == vertex.name }
        vertexToViewMap.remove(vertex)
    }

    private fun registerTransition(transition: Transition) {
        val transitionView = AdjacencyMatrixTransitionView(transition)
        controller.registerAutomatonElementView(transitionView)
        transitionToViewMap[transition] = transitionView
        transitionsByVertices.find { it.source == transition.source }.apply {
            val list = this!!.transitions[transition.target.name]!!.value
            this.transitions[transition.target.name]!!.set(list + transition)
        }
    }

    private fun unregisterTransition(transition: Transition) {
        transitionsByVertices.find { it.source == transition.source }.apply {
            val list = this!!.transitions[transition.target.name]!!.value
            this.transitions[transition.target.name]!!.set(list - transition)
        }
        transitionToViewMap.remove(transition)
    }

    fun computeCellWidth(textLength: Int) = SYMBOL_WIDTH * textLength + ADDITIONAL_GAP

    companion object {
        const val SYMBOL_WIDTH = 23.0
        const val ADDITIONAL_GAP = 30.0
    }
}