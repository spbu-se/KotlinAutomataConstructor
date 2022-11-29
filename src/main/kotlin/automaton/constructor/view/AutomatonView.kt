package automaton.constructor.view

import automaton.constructor.controller.UndoRedoController
import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.module.descriptionBinding
import automaton.constructor.model.module.problems
import automaton.constructor.utils.I18N
import automaton.constructor.utils.SettingsEditor
import automaton.constructor.utils.customizedZoomScrollPane
import automaton.constructor.utils.nonNullObjectBinding
import javafx.beans.binding.Bindings.not
import javafx.scene.input.KeyEvent
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.TextAlignment
import tornadofx.*

// TODO extract AutomatonDescriptionProviderView and ProblemDetectorView
class AutomatonView(val automaton: Automaton, automatonViewContext: AutomatonViewContext) : Pane() {
    val automatonGraphView = AutomatonGraphView(automaton, automatonViewContext)
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
        customizedZoomScrollPane { add(automatonGraphView) }
        val settingsEditor = SettingsEditor().apply {
            settingsProperty.bind(automatonGraphView.controller.lastSelectedElementProperty.nonNullObjectBinding {
                it?.getSettings()
            })
            editingDisabledProperty.bind(not(automaton.allowsModificationsByUserProperty))
            visibleWhen(automaton.isOutputOfTransformationProperty.booleanBinding { it == null })
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
            maxWidthProperty().bind((this@AutomatonView.widthProperty() - 20.0) / 2.0)
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
        label {
            layoutX = 10.0
            layoutYProperty().bind(this@AutomatonView.heightProperty() - heightProperty())
            maxWidthProperty().bind((this@AutomatonView.widthProperty() - 20.0) / 2.0)
            font = Font.font(16.0)
            text = I18N.messages.getString("AutomatonView.UseRightClickToAddElements")
            visibleWhen(automaton.isOutputOfTransformationProperty.booleanBinding { it == null })
        }
    }
}
