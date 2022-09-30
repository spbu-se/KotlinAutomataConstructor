package automaton.constructor.view

import automaton.constructor.controller.UndoRedoController
import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.module.descriptionBinding
import automaton.constructor.model.module.executor.executor
import automaton.constructor.model.module.problems
import automaton.constructor.utils.I18N
import automaton.constructor.utils.SettingsEditor
import automaton.constructor.utils.customizedZoomScrollPane
import automaton.constructor.utils.nonNullObjectBinding
import automaton.constructor.view.module.executor.ExecutorView
import automaton.constructor.view.module.executor.tree.ExecutionTreeView
import javafx.scene.control.SplitPane
import javafx.scene.input.KeyEvent
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.TextAlignment
import tornadofx.*

// TODO extract AutomatonDescriptionProviderView and ProblemDetectorView
class AutomatonView(val automaton: Automaton, private val isMinimalistic: Boolean = false) : SplitPane() {
    val automatonGraphView = AutomatonGraphView(automaton)
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
        pane {
            customizedZoomScrollPane { add(automatonGraphView) }
            val settingsEditor = SettingsEditor().apply {
                settingsProperty.bind(automatonGraphView.controller.lastSelectedElementProperty.nonNullObjectBinding {
                    it?.getSettings()
                })
            }
            add(settingsEditor)
            label {
                isWrapText = true
                layoutXProperty().bind(this@pane.widthProperty() - widthProperty() - 10.0)
                maxWidthProperty().bind(this@pane.widthProperty() - settingsEditor.widthProperty() - 20.0)
                font = Font.font(16.0)
                textProperty().bind(automaton.descriptionBinding)
            }
            val executorViewHeightProperty = if (!isMinimalistic) {
                val executorView = ExecutorView(automaton.executor)
                add(executorView)
                executorView.apply {
                    prefWidthProperty().bind(this@pane.widthProperty())
                    layoutYProperty().bind(this@pane.heightProperty() - heightProperty())
                }
                executorView.heightProperty()
            } else 0.0.toProperty()
            label {
                layoutXProperty().bind(this@pane.widthProperty() - widthProperty() - 10.0)
                layoutYProperty().bind(this@pane.heightProperty() - executorViewHeightProperty - heightProperty())
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
            }
        }
        if (!isMinimalistic)
            ExecutionTreeView(automaton.executor).also { executionTreeView ->
                executionTreeView.visibleProperty().onChange {
                    if (it) {
                        add(executionTreeView)
                        setDividerPosition(0, 0.7)
                    } else items.removeLast()
                }
            }
    }
}
