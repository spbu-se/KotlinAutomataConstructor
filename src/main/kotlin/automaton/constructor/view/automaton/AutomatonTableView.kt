package automaton.constructor.view.automaton

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
import automaton.constructor.view.AutomatonBasicVertexView
import automaton.constructor.view.AutomatonViewContext
import automaton.constructor.view.TableTransitionView
import javafx.beans.property.ReadOnlyDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.SetChangeListener
import javafx.geometry.Insets
import javafx.scene.control.ListCell
import javafx.scene.control.TableCell
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.input.MouseButton
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

    private fun registerVertex(vertex: AutomatonVertex): AutomatonBasicVertexView {
        val vertexView = AutomatonBasicVertexView(vertex)
        table.controller.registerAutomatonElementView(vertexView)
        if (vertex is BuildingBlock) {
            vertexView.hoverableTooltip(stopManagingOnInteraction = true) {
                Pane().apply {
                    minWidth = table.scene.window.width / 1.5
                    minHeight = table.scene.window.height / 1.5
                    maxWidth = table.scene.window.width / 1.5
                    maxHeight = table.scene.window.height / 1.5
                    val subAutomatonView = table.automatonViewContext.getAutomatonView(vertex.subAutomaton)
                    add(subAutomatonView)
                    subAutomatonView.fitToParentSize()
                }
            }
        }
        return vertexView
    }

    override fun updateItem(item: AutomatonVertex?, empty: Boolean) {
        super.updateItem(item, empty)
        if (item != null) {
            val vertexView = registerVertex(item)
            colourProperty.bind(vertexView.colourProperty)
            this.style = "-fx-background-color: ${colour};"
            colourProperty.addListener(ChangeListener { _, _, newValue ->
                this.style = "-fx-background-color: ${newValue};"
            })
            graphic = vertexView
        } else {
            this.style = "-fx-background-color: none;"
            graphic = null
        }
    }
}

class TransitionsCell<T: TableTransitionView, M: TransitionMap>(
    private val transitionToViewMap: MutableMap<Transition, T>
): TableCell<M, List<Transition>>() {
    override fun updateItem(item: List<Transition>?, empty: Boolean) {
        super.updateItem(item, empty)
        graphic = if (item != null) {
            VBox().apply {
                item.forEach {
                    add(transitionToViewMap[it]!!)
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

    override val root = vbox(5.0) {
        label(I18N.messages.getString("NewTransitionPopup.Question"))
        borderpane {
            class VertexCell: ListCell<AutomatonVertex>() {
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
            left = hbox(5.0) {
                label(I18N.messages.getString("NewTransitionPopup.Source"))
                val sourceBox = combobox(source, automaton.vertices.toList())
                sourceBox.setCellFactory { VertexCell() }
                sourceBox.buttonCell = VertexCell()
            }
            right = hbox(2.0) {
                label(I18N.messages.getString("NewTransitionPopup.Target"))
                val targetBox = combobox(target, automaton.vertices.toList())
                targetBox.setCellFactory { VertexCell() }
                targetBox.buttonCell = VertexCell()
            }
        }
        button(I18N.messages.getString("NewTransitionPopup.Add")) {
            action {
                automaton.addTransition(source.value, target.value)
            }
        }
        padding = Insets(5.0, 5.0, 5.0, 5.0)
        minWidth = 280.0
    }
}

abstract class AutomatonTableView<T: TableTransitionView, M: TransitionMap>(
    val automaton: Automaton,
    val automatonViewContext: AutomatonViewContext,
    private val tablePrefWidth: ReadOnlyDoubleProperty,
    private val tablePrefHeight: ReadOnlyDoubleProperty
): Pane() {
    val transitionsByVertices = observableListOf<M>()
    val table = TableView(transitionsByVertices)
    val sourceColumn = TableColumn<M, AutomatonVertex>()
    val controller = AutomatonRepresentationController(automaton, automatonViewContext)
    val transitionToViewMap = mutableMapOf<Transition, T>()
    init {
        automaton.vertices.addListener(SetChangeListener {
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
                        if (automaton.allowsModificationsByUser) {
                            automaton.addState(
                                position = javafx.geometry.Point2D(
                                    500_000.0 + Random.nextDouble(-500.0, 500.0),
                                    500_000.0 + Random.nextDouble(-500.0, 500.0)
                                )
                            )
                        }
                    }
                }
                if (automaton.allowsBuildingBlocks) {
                    button(I18N.messages.getString("AutomatonTableView.AddBuildingBlock")) {
                        action {
                            if (automaton.allowsModificationsByUser) {
                                automaton.addBuildingBlock()
                            }
                        }
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
                    }
                }
                button(I18N.messages.getString("AutomatonTableView.AddTransition")) {
                    action {
                        if (automaton.allowsModificationsByUser) {
                            val scope = Scope()
                            val newTransitionWindow =
                                find<NewTransitionPopup>(scope, mapOf(NewTransitionPopup::automaton to automaton))
                            newTransitionWindow.title = I18N.messages.getString("NewTransitionPopup.Title")
                            newTransitionWindow.openWindow()
                        }
                    }
                }
            }
        }

        sourceColumn.cellValueFactory = PropertyValueFactory("source")
        sourceColumn.setCellFactory { VertexCell(this) }
        table.columns.add(sourceColumn)
        table.setOnMouseClicked {
            if (it.button == MouseButton.PRIMARY) controller.clearSelection()
        }

        table.style {
            fontSize = 16.0.px
        }
    }

    abstract fun unregisterVertex(vertex: AutomatonVertex)

    abstract fun registerTransition(transition: Transition)

    abstract fun unregisterTransition(transition: Transition)

    fun enableProperResizing() {
        table.prefWidthProperty().bind(tablePrefWidth)
        table.prefHeightProperty().bind(tablePrefHeight)
    }
}
