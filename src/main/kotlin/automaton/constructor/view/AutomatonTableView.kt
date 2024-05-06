package automaton.constructor.view

import automaton.constructor.controller.AutomatonRepresentationController
import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.automaton.allowsBuildingBlocks
import automaton.constructor.model.data.addContent
import automaton.constructor.model.element.AutomatonVertex
import automaton.constructor.model.element.Transition
import automaton.constructor.utils.I18N
import automaton.constructor.utils.addOnSuccess
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ListChangeListener
import javafx.collections.SetChangeListener
import javafx.scene.control.ListCell
import javafx.scene.control.TableCell
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import tornadofx.*

class TransitionMap(
    val source: AutomatonVertex,
    val transitions: MutableMap<String, SimpleObjectProperty<List<Transition>>> = mutableMapOf()
)

class SourceCell<T: TableTransitionView>(
    private val table: AutomatonTableView<T>
): TableCell<TransitionMap, AutomatonVertex>() {
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

class TransitionsCell<T: TableTransitionView>(
    private val table: AutomatonTableView<T>
): TableCell<TransitionMap, List<Transition>>() {
    override fun updateItem(item: List<Transition>?, empty: Boolean) {
        super.updateItem(item, empty)
        graphic = if (item != null) {
            VBox().apply {
                item.forEach {
                    add(table.transitionToViewMap[it]!!)
                    val prefWidth = table.computeCellWidth(table.transitionToViewMap[it]!!.textLength)
                    this@TransitionsCell.tableColumn.prefWidth =
                        maxOf(this@TransitionsCell.tableColumn.prefWidth, prefWidth)
                }
            }
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

abstract class AutomatonTableView<T: TableTransitionView>(
    private val automaton: Automaton,
    val automatonViewContext: AutomatonViewContext
): Pane() {
    val transitionsByVertices = observableListOf<TransitionMap>()
    private val table = TableView(transitionsByVertices)
    val sourceColumn = TableColumn<TransitionMap, AutomatonVertex>()
    val transitionsColumns = observableListOf<TableColumn<TransitionMap, List<Transition>>>()
    val filtersCount = mutableMapOf<String, Int>()
    val controller = AutomatonRepresentationController(automaton, automatonViewContext)
    val vertexToViewMap = mutableMapOf<AutomatonVertex, AutomatonBasicVertexView>()
    val transitionToViewMap = mutableMapOf<Transition, T>()
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
                    filtersCount.keys.forEach { filter ->
                        addedMap.transitions[filter] = SimpleObjectProperty(listOf())
                    }
                }
            }
        })
        transitionsColumns.addListener(ListChangeListener {
            while (it.next()) {
                if (it.wasAdded()) {
                    val addedColumn = it.addedSubList.first()
                    filtersCount[addedColumn.text] = 0
                    transitionsByVertices.forEach {
                        it.transitions[addedColumn.text] = SimpleObjectProperty(listOf())
                    }
                    addedColumn.setCellValueFactory { p0 ->
                        p0!!.value.transitions[addedColumn.text]!!
                    }
                    addedColumn.setCellFactory { TransitionsCell(this) }
                    addedColumn.minWidth = computeCellWidth(addedColumn.text.length)
                    table.columns.add(addedColumn)
                }
                if (it.wasRemoved()) {
                    val removedColumn = it.removed.first()
                    filtersCount.remove(removedColumn.text)
                    table.columns.remove(removedColumn)
                }
            }
        })

        vbox {
            add(table)
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
        sourceColumn.cellValueFactory = PropertyValueFactory("source")
        sourceColumn.setCellFactory { SourceCell(this) }
        sourceColumn.minWidth = 150.0
        table.columns.add(0, sourceColumn)

        table.style {
            minWidth = 1900.0.px
            minHeight = 1000.0.px
            fontSize = 40.0.px
        }
    }

    abstract fun registerVertex(vertex: AutomatonVertex)

    abstract fun unregisterVertex(vertex: AutomatonVertex)

    abstract fun registerTransition(transition: Transition)

    abstract fun unregisterTransition(transition: Transition)

    fun computeCellWidth(textLength: Int) = SYMBOL_WIDTH * textLength + ADDITIONAL_GAP

    companion object {
        const val SYMBOL_WIDTH = 23.0
        const val ADDITIONAL_GAP = 30.0
    }
}