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
import kotlin.random.Random

interface TransitionMap

class VertexCell<T: TableTransitionView, M: TransitionMap>(
    private val table: AutomatonTableView<T, M>
): TableCell<M, AutomatonVertex>() {
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

class TransitionsCell<T: TableTransitionView, M: TransitionMap>(
    private val table: AutomatonTableView<T, M>
): TableCell<M, List<Transition>>() {
    override fun updateItem(item: List<Transition>?, empty: Boolean) {
        super.updateItem(item, empty)
        graphic = if (item != null) {
            VBox().apply {
                item.forEach {
                    add(table.transitionToViewMap[it]!!)
                }
            }
        } else {
            null
        }
    }
}

class NewTransitionPopup: Fragment() {
    val automaton: Automaton by param()
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

abstract class AutomatonTableView<T: TableTransitionView, M: TransitionMap>(
    val automaton: Automaton,
    val automatonViewContext: AutomatonViewContext
): Pane() {
    val transitionsByVertices = observableListOf<M>()
    val table = TableView(transitionsByVertices)
    val sourceColumn = TableColumn<M, AutomatonVertex>()
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

        vbox {
            add(table)
            hbox {
                button(I18N.messages.getString("AutomatonTableView.AddState")) {
                    action {
                        automaton.addState(position = javafx.geometry.Point2D(
                            500_000.0 + Random.nextDouble(-500.0, 500.0),
                            500_000.0 + Random.nextDouble(-500.0, 500.0)
                        ))
                    }
                    style = "-fx-font-size:30"
                }
                if (automaton.allowsBuildingBlocks) {
                    button(I18N.messages.getString("AutomatonTableView.AddBuildingBlock")) {
                        action {
                            automaton.addBuildingBlock()
                        }
                        style = "-fx-font-size:30"
                    }
                    button(I18N.messages.getString("AutomatonTableView.CopyBuildingBlock")) {
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
                        style = "-fx-font-size:30"
                    }
                }
                button(I18N.messages.getString("AutomatonTableView.AddTransition")) {
                    action {
                        val scope = Scope()
                        val newTransitionWindow = find<NewTransitionPopup>(scope, mapOf(NewTransitionPopup::automaton to automaton))
                        newTransitionWindow.openWindow()
                    }
                    style = "-fx-font-size:30"
                }
            }
        }

        sourceColumn.cellValueFactory = PropertyValueFactory("source")
        sourceColumn.setCellFactory { VertexCell(this) }
        table.columns.add(sourceColumn)

        table.style {
            minWidth = TABLE_INIT_WIDTH.px
            fontSize = 40.0.px
        }
    }

    abstract fun registerVertex(vertex: AutomatonVertex)

    abstract fun unregisterVertex(vertex: AutomatonVertex)

    abstract fun registerTransition(transition: Transition)

    abstract fun unregisterTransition(transition: Transition)

    fun enableProperResizing() {
        table.prefWidthProperty().bind(automatonViewContext.tablePrefWidth * 2.5)
        table.prefHeightProperty().bind(automatonViewContext.tablePrefHeight * 2.5 - 245.0)
    }

    companion object {
        const val TABLE_INIT_WIDTH = 1000.0
    }
}