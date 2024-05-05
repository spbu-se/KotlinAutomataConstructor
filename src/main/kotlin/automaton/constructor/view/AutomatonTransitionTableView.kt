package automaton.constructor.view

import automaton.constructor.controller.AutomatonRepresentationController
import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.automaton.allowsBuildingBlocks
import automaton.constructor.model.data.addContent
import automaton.constructor.model.element.AutomatonVertex
import automaton.constructor.model.element.BuildingBlock
import automaton.constructor.model.element.Transition
import automaton.constructor.utils.I18N
import automaton.constructor.utils.addOnSuccess
import automaton.constructor.utils.hoverableTooltip
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ListChangeListener
import javafx.collections.SetChangeListener
import javafx.scene.control.*
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import tornadofx.*

class TransitionMap(
    val source: AutomatonVertex,
    val transitions: MutableMap<String, SimpleObjectProperty<List<Transition>>> = mutableMapOf()
)

class StateCell(private val table: AutomatonTransitionTableView): TableCell<TransitionMap, AutomatonVertex>() {
    private val colourProperty = SimpleStringProperty("")
    private var colour by colourProperty
    override fun updateItem(item: AutomatonVertex?, empty: Boolean) {
        super.updateItem(item, empty)
        if (item != null) {
            colourProperty.bind(table.vertexToViewMap[item]!!.colourProperty)
            this.style = "-fx-background-color: ${colour};"
            colourProperty.addListener(ChangeListener { _, _, newValue ->
                this.style = "-fx-background-color: ${newValue};"
            })
            graphic = table.vertexToViewMap[item]
        } else {
            this.style = "-fx-background-color: white;"
            graphic = null
        }
    }
}

class InputCell(private val table: AutomatonTransitionTableView): TableCell<TransitionMap, List<Transition>>() {
    override fun updateItem(item: List<Transition>?, empty: Boolean) {
        super.updateItem(item, empty)
        graphic = if (item != null) {
            val vbox = VBox()
            item.forEach {
                vbox.add(table.transitionToViewMap[it]!!)
                val prefWidth = table.computeCellWidth(table.transitionToViewMap[it]!!.textLength)
                this.tableColumn.prefWidth = maxOf(this.tableColumn.prefWidth, prefWidth)
            }
            vbox
        } else {
            null
        }
    }
}

class NewTransitionPopup(val automaton: Automaton): Fragment() {
    val source = SimpleObjectProperty<AutomatonVertex>()
    val target = SimpleObjectProperty<AutomatonVertex>()
    override val root = vbox {
        label("What transition would you like to add?")
        hbox {
            label("Source vertex")
            val sourceBox = combobox(source, automaton.vertices.toList())

            class VertexCell : ListCell<AutomatonVertex>() {
                override fun updateItem(item: AutomatonVertex?, empty: Boolean) {
                    super.updateItem(item, empty)
                    graphic = if (item != null) {
                        label(item.name) {
                            textFill = Color.BLACK
                        }
                    } else {
                        null
                    }
                }
            }
            sourceBox.setCellFactory { VertexCell() }
            sourceBox.buttonCell = VertexCell()
            label("Target vertex")
            val targetBox = combobox(target, automaton.vertices.toList())
            targetBox.setCellFactory { VertexCell() }
            targetBox.buttonCell = VertexCell()
        }
        button("Add") {
            action {
                automaton.addTransition(source.value, target.value)
            }
        }
    }
}

class AutomatonTransitionTableView(
    val automaton: Automaton,
    private val automatonViewContext: AutomatonViewContext
) : Pane() {
    private val transitionsByVertices = observableListOf<TransitionMap>()
    private val transitionTable = TableView(transitionsByVertices)
    private val stateColumn = TableColumn<TransitionMap, AutomatonVertex>("State")
    private val inputColumns = observableListOf<TableColumn<TransitionMap, List<Transition>>>()
    private val inputsCount = mutableMapOf<String, Int>()
    val controller = AutomatonRepresentationController(automaton, automatonViewContext)
    val vertexToViewMap = mutableMapOf<AutomatonVertex, AutomatonBasicVertexView>()
    val transitionToViewMap = mutableMapOf<Transition, TransitionTableTransitionView>()

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
                    inputsCount.keys.forEach { input ->
                        addedMap.transitions[input] = SimpleObjectProperty(listOf())
                    }
                }
            }
        })
        inputColumns.addListener(ListChangeListener {
            while (it.next()) {
                if (it.wasAdded()) {
                    val addedColumn = it.addedSubList.first()
                    inputsCount[addedColumn.text] = 0
                    transitionsByVertices.forEach { map ->
                        map.transitions[addedColumn.text] = SimpleObjectProperty(mutableListOf())
                    }
                    addedColumn.setCellValueFactory { p0 ->
                        p0!!.value.transitions[addedColumn.text]!!
                    }
                    addedColumn.setCellFactory { InputCell(this) }
                    addedColumn.minWidth = computeCellWidth(addedColumn.text.length)
                    transitionTable.columns.add(addedColumn)
                }
                if (it.wasRemoved()) {
                    val removedColumn = it.removed.first()
                    inputsCount.remove(removedColumn.text)
                    transitionTable.columns.remove(removedColumn)
                }
            }
        })

        vbox {
            add(transitionTable)
            hbox {
                vbox {
                    button("Add state") {
                        action {
                            automaton.addState()
                        }
                        style = "-fx-font-size:30"
                    }
                    if (automaton.allowsBuildingBlocks) {
                        button("Add empty building block") {
                            action {
                                automaton.addBuildingBlock()
                            }
                        }
                        button("Copy building block from file") {
                            action {
                                if (!automaton.allowsModificationsByUser) return@action
                                val file = automatonViewContext.fileController.chooseFile(
                                    I18N.messages.getString("MainView.File.Open"),
                                    FileChooserMode.Single
                                ) ?: return@action
                                automatonViewContext.fileController.loadAsync(file) addOnSuccess { (type, vertices, transitions, edges) ->
                                    if (type != automaton.getTypeData()) error(
                                        I18N.messages.getString("AutomatonGraphController.BuildingBlockLoadingFailed"),
                                        I18N.messages.getString("AutomatonGraphController.IncompatibleAutomatonType"),
                                        owner = automatonViewContext.uiComponent.currentWindow
                                    )
                                    else {
                                        automaton.addBuildingBlock().apply {
                                            subAutomaton.addContent(vertices, transitions, edges)
                                            name = file.nameWithoutExtension
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                button("Add transition") {
                    action {
                        find<NewTransitionPopup>().openWindow()
                    }
                    style = "-fx-font-size:30"
                }
            }
        }

        automaton.vertices.forEach { registerVertex(it) }
        automaton.transitions.forEach { registerTransition(it) }
        stateColumn.cellValueFactory = PropertyValueFactory("source")
        stateColumn.setCellFactory { StateCell(this) }
        stateColumn.minWidth = 150.0
        transitionTable.columns.add(0, stateColumn)

        transitionTable.style {
            minWidth = 1900.0.px
            minHeight = 1000.0.px
            fontSize = 40.0.px
        }
    }

    private fun registerVertex(vertex: AutomatonVertex) {
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

    private fun unregisterVertex(vertex: AutomatonVertex) {
        transitionsByVertices.removeAll { it.source == vertex }
        vertexToViewMap.remove(vertex)
    }

    private fun registerTransition(transition: Transition) {
        val transitionView = TransitionTableTransitionView(transition)
        controller.registerAutomatonElementView(transitionView)
        transitionToViewMap[transition] = transitionView
        transition.filtersTextBinding.addListener { _, oldValue, _ ->
            deleteTransitionFromTable(transition, oldValue)
            addTransitionToTable(transition)
        }
        addTransitionToTable(transition)
    }

    private fun unregisterTransition(transition: Transition) {
        deleteTransitionFromTable(transition)
        transitionToViewMap.remove(transition)
    }
    
    private fun addTransitionToTable(transition: Transition) {
        if (!inputsCount.contains(transition.filtersText)) {
            inputColumns.add(TableColumn(transition.filtersText))
        }

        var transitionMap = transitionsByVertices.find { it.source == transition.source }
        if (transitionMap == null) {
            transitionMap = TransitionMap(transition.source)
            transitionsByVertices.add(transitionMap)
        }
        val list = transitionMap.transitions[transition.filtersText]!!.get()
        transitionMap.transitions[transition.filtersText]!!.set(list + transition)
        inputsCount[transition.filtersText] = inputsCount[transition.filtersText]!! + 1
    }
    
    private fun deleteTransitionFromTable(transition: Transition, filtersText: String = transition.filtersText) {
        transitionsByVertices.find { map ->
            map.source == transition.source
        }.apply {
            val list = this!!.transitions[filtersText]!!.value
            this.transitions[filtersText]!!.set(list - transition)
        }
        inputsCount[filtersText] = inputsCount[filtersText]!! - 1
        if (inputsCount[filtersText] == 0) {
            inputColumns.removeAll { it.text == filtersText }
        }
    }

    fun computeCellWidth(textLength: Int) = SYMBOL_WIDTH * textLength + ADDITIONAL_GAP

    companion object {
        const val SYMBOL_WIDTH = 23.0
        const val ADDITIONAL_GAP = 30.0
    }
}