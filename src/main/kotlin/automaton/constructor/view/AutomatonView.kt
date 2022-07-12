package automaton.constructor.view

import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.module.descriptionBinding
import automaton.constructor.model.module.executor.executor
import automaton.constructor.model.module.problems
import automaton.constructor.utils.SettingsEditor
import automaton.constructor.utils.customizedZoomScrollPane
import automaton.constructor.utils.nonNullObjectBinding
import automaton.constructor.view.module.executor.ExecutorView
import automaton.constructor.view.module.executor.tree.ExecutionTreeView
import javafx.scene.control.SplitPane
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.TextAlignment
import tornadofx.*

// TODO extract AutomatonDescriptionProviderView and ProblemDetectorView
class AutomatonView(val automaton: Automaton, val view: View) : SplitPane() {
    val automatonGraphView = AutomatonGraphView(automaton)

    init {
        pane {
            customizedZoomScrollPane { add(automatonGraphView) }
            add(SettingsEditor().apply {
                settingsProperty.bind(automatonGraphView.controller.lastSelectedElementProperty.nonNullObjectBinding {
                    it?.getSettings()
                })
            })
            label {
                layoutXProperty().bind(this@pane.widthProperty() - widthProperty() - 10.0)
                font = Font.font(16.0)
                textProperty().bind(automaton.descriptionBinding)
            }
            val executorView = ExecutorView(automaton.executor, view)
            add(executorView)
            executorView.apply {
                prefWidthProperty().bind(this@pane.widthProperty())
                layoutYProperty().bind(this@pane.heightProperty() - heightProperty())
            }
            label {
                layoutXProperty().bind(this@pane.widthProperty() - widthProperty() - 10.0)
                layoutYProperty().bind(this@pane.heightProperty() - executorView.heightProperty() - heightProperty())
                font = Font.font(16.0)
                textFill = Color.DARKRED
                textAlignment = TextAlignment.RIGHT
                fun updateText() {
                    text = if (automaton.problems.isEmpty()) ""
                    else automaton.problems.joinToString(prefix = "Problems:\n", separator = "\n") { it.message }
                }
                updateText()
                automaton.problems.onChange { updateText() }
            }
        }
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
