package automaton.constructor.view

import automaton.constructor.controller.UndoRedoController
import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.module.descriptionBinding
import automaton.constructor.model.module.problems
import automaton.constructor.utils.I18N
import automaton.constructor.utils.SettingsEditor
import automaton.constructor.utils.customizedZoomScrollPane
import javafx.beans.binding.Bindings.not
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
    val automatonTransitionTableView = AutomatonTransitionTableView(automaton, automatonViewContext)
    val automatonAdjacencyMatrixView = AutomatonAdjacencyMatrixView(automaton, automatonViewContext)
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
        val graphTab = customizedZoomScrollPane { add(automatonGraphView) }
        val tableTab = customizedZoomScrollPane { add(automatonTransitionTableView) }
        val matrixTab = customizedZoomScrollPane { add(automatonAdjacencyMatrixView) }
        tabpane {
            tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE
            tab(I18N.messages.getString("AutomatonView.Graph")) {
                add(graphTab)
            }
            tab(I18N.messages.getString("AutomatonView.Table")) {
                add(tableTab)
            }
            tab(I18N.messages.getString("AutomatonView.Matrix")) {
                add(matrixTab)
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
            layoutY = 23.5
        }
        add(settingsEditor)
        label {
            isWrapText = true
            layoutXProperty().bind(this@AutomatonView.widthProperty() - widthProperty() - 10.0)
            maxWidthProperty().bind(this@AutomatonView.widthProperty() - settingsEditor.widthProperty() - 20.0)
            font = Font.font(16.0)
            textProperty().bind(automaton.descriptionBinding)
            visibleWhen(automaton.isOutputOfTransformationProperty.booleanBinding { it == null })
        }
        label {
            layoutXProperty().bind(this@AutomatonView.widthProperty() - widthProperty() - 10.0)
            layoutYProperty().bind(this@AutomatonView.heightProperty() - heightProperty())
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
