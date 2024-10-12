package automaton.constructor.view

import automaton.constructor.controller.UndoRedoController
import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.module.descriptionBinding
import automaton.constructor.model.module.problems
import automaton.constructor.utils.I18N
import automaton.constructor.utils.SettingsEditor
import automaton.constructor.utils.customizedZoomScrollPane
import automaton.constructor.view.automaton.AutomatonAdjacencyMatrixView
import automaton.constructor.view.automaton.AutomatonGraphView
import automaton.constructor.view.automaton.AutomatonTransitionTableView
import javafx.beans.binding.Bindings.not
import javafx.beans.property.SimpleDoubleProperty
import javafx.scene.control.ScrollPane
import javafx.scene.control.TabPane
import javafx.scene.input.KeyEvent
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.TextAlignment
import tornadofx.*

// TODO extract AutomatonDescriptionProviderView and ProblemDetectorView
class AutomatonView(val automaton: Automaton, automatonViewContext: AutomatonViewContext) : Pane() {
    val automatonGraphView = AutomatonGraphView(automaton, automatonViewContext)
    private val tablePrefWidth = SimpleDoubleProperty().also { it.bind(this.widthProperty()) }
    val tablePrefHeight = SimpleDoubleProperty().also { it.bind(this.heightProperty() - 48.0) }
    private val automatonTransitionTableView = AutomatonTransitionTableView(
        automaton, automatonViewContext, tablePrefWidth, tablePrefHeight)
    private val automatonAdjacencyMatrixView = AutomatonAdjacencyMatrixView(
        automaton, automatonViewContext, tablePrefWidth, tablePrefHeight)
    val undoRedoController = UndoRedoController(this)

    init {
        addEventFilter(KeyEvent.KEY_PRESSED) { event ->
            when {
                UndoRedoController.UNDO_COMBO.match(event) -> undoRedoController.onUndo()
                UndoRedoController.REDO_COMBO.match(event) -> undoRedoController.onRedo()
                else -> return@addEventFilter
            }
            event.consume()
        }

        automatonGraphView.controller.lastSelectedElementProperty.addListener(ChangeListener { _, _, newValue ->
            automatonTransitionTableView.controller.lastSelectedElement = newValue
            automatonAdjacencyMatrixView.controller.lastSelectedElement = newValue
        })
        automatonTransitionTableView.controller.lastSelectedElementProperty.addListener(ChangeListener { _, _, newValue ->
            automatonGraphView.controller.lastSelectedElement = newValue
            automatonAdjacencyMatrixView.controller.lastSelectedElement = newValue
        })
        automatonAdjacencyMatrixView.controller.lastSelectedElementProperty.addListener(ChangeListener { _, _, newValue ->
            automatonGraphView.controller.lastSelectedElement = newValue
            automatonTransitionTableView.controller.lastSelectedElement = newValue
        })
        val graphPane = customizedZoomScrollPane { add(automatonGraphView) }
        val tablePane = ScrollPane().also { it.add(automatonTransitionTableView) }
        val matrixPane = ScrollPane().also { it.add(automatonAdjacencyMatrixView) }
        tabpane {
            tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE
            tab(I18N.messages.getString("AutomatonView.Graph")) {
                add(graphPane)
            }
            val tableTab = tab(I18N.messages.getString("AutomatonView.Table")) {
                add(tablePane)
            }
            val matrixTab = tab(I18N.messages.getString("AutomatonView.Matrix")) {
                add(matrixPane)
            }
            selectionModel.selectedItemProperty().addListener { _, _, newValue ->
                if (newValue == tableTab) {
                    automatonTransitionTableView.enableProperResizing()
                }
                if (newValue == matrixTab) {
                    automatonAdjacencyMatrixView.enableProperResizing()
                }
            }
        }
        val settingsEditor = SettingsEditor().apply {
            automatonGraphView.controller.lastSelectedElementProperty.addListener(ChangeListener { _, _, newValue ->
                settingsProperty.set(newValue?.getSettings())
            })
            automatonTransitionTableView.controller.lastSelectedElementProperty.addListener(ChangeListener { _, _, newValue ->
                settingsProperty.set(newValue?.getSettings())

            })
            automatonAdjacencyMatrixView.controller.lastSelectedElementProperty.addListener(ChangeListener { _, _, newValue ->
                settingsProperty.set(newValue?.getSettings())
            })
            editingDisabledProperty.bind(not(automaton.allowsModificationsByUserProperty))
            visibleWhen(automaton.isOutputOfTransformationProperty.booleanBinding { it == null })
            layoutY = 23.8
        }
        add(settingsEditor)
        label {
            isWrapText = true
            layoutXProperty().bind(this@AutomatonView.widthProperty() - widthProperty() - 10.0)
            val tabWidth = 700.0
            maxWidthProperty().bind(this@AutomatonView.widthProperty() - tabWidth - 20.0)
            maxHeight = 30.0
            font = Font.font(16.0)
            textProperty().bind(automaton.descriptionBinding)
            visibleWhen(automaton.isOutputOfTransformationProperty.booleanBinding { it == null })
        }
        label {
            layoutXProperty().bind(this@AutomatonView.widthProperty() - widthProperty() - 10.0)
            layoutYProperty().bind(this@AutomatonView.heightProperty() - heightProperty() - 45.0)
            val buttonsWidth = 700.0
            maxWidthProperty().bind(this@AutomatonView.widthProperty() - buttonsWidth - 20.0)
            font = Font.font(16.0)
            textFill = Color.DARKRED
            textAlignment = TextAlignment.RIGHT
            fun updateText() {
                text = if (automaton.problems.isEmpty()) ""
                else automaton.problems.joinToString(
                    prefix = I18N.messages.getString("AutomatonView.Problems"), separator = "\n"
                ) { it.message }
            }
            updateText()
            automaton.problems.onChange { updateText() }
            visibleWhen(automaton.isOutputOfTransformationProperty.booleanBinding { it == null })
        }
    }
}
