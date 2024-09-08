package automaton.constructor.view.automaton

import automaton.constructor.controller.AutomatonRepresentationController
import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.automaton.allowsBuildingBlocks
import automaton.constructor.model.element.AutomatonVertex
import automaton.constructor.model.element.BuildingBlock
import automaton.constructor.model.element.Transition
import automaton.constructor.utils.*
import automaton.constructor.view.AutomatonBasicVertexView
import automaton.constructor.view.AutomatonViewContext
import automaton.constructor.view.TableTransitionView
import javafx.beans.property.SimpleDoubleProperty
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
            width = table.scene.window.width / 1.5
            height = table.scene.window.height / 1.5
            setBuildingBlockToolTip(vertex, vertexView, table.automatonViewContext, width, height)
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
    val tablePrefWidth: SimpleDoubleProperty,
    val tablePrefHeight: SimpleDoubleProperty
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
                            copyBuildingBlock(automaton, automatonViewContext)
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
            //minWidth = TABLE_INIT_WIDTH.px
            fontSize = 16.0.px
        }
    }

    abstract fun unregisterVertex(vertex: AutomatonVertex)

    abstract fun registerTransition(transition: Transition)

    abstract fun unregisterTransition(transition: Transition)

    fun enableProperResizing() {
        table.prefWidthProperty().bind(tablePrefWidth)
        table.prefHeightProperty().bind(tablePrefHeight - 73.0)
    }

    companion object {
        const val TABLE_INIT_WIDTH = 1000.0
    }
}
