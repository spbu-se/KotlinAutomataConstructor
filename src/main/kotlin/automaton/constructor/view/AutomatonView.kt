package automaton.constructor.view

import automaton.constructor.controller.AutomatonController
import automaton.constructor.model.Automaton
import automaton.constructor.model.module.descriptionBinding
import automaton.constructor.model.module.executor.executor
import automaton.constructor.model.module.problems
import automaton.constructor.utils.SettingsEditor
import automaton.constructor.utils.nonNullObjectBinding
import automaton.constructor.utils.zoomScrollPane
import automaton.constructor.view.module.executor.ExecutorView
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.TextAlignment
import tornadofx.*

private const val DEFAULT_GRAPH_SCALE = 0.4

// TODO extract AutomatonDescriptionProviderView and ProblemDetectorView
class AutomatonView(val automaton: Automaton, val view: View) : Pane() {
    val controller = AutomatonController(automaton)
    private val automatonGraphView = AutomatonGraphView(controller)

    init {
        zoomScrollPane(automatonGraphView, DEFAULT_GRAPH_SCALE) {
            style {
                focusColor = Color.TRANSPARENT
            }
            hvalue = 0.5
            vvalue = 0.5
            fitToWidth(this@AutomatonView)
            fitToHeight(this@AutomatonView)
            prefWidthProperty().bind(this@AutomatonView.widthProperty())
            prefHeightProperty().bind(this@AutomatonView.heightProperty())
        }
        add(SettingsEditor().apply {
            settingsProperty.bind(controller.selectedSettingsHolderProperty.nonNullObjectBinding {
                it?.getSettings()
            })
        })
        label {
            layoutXProperty().bind(this@AutomatonView.widthProperty() - widthProperty() - 10.0)
            layoutY = 10.0
            font = Font.font(16.0)
            textProperty().bind(automaton.descriptionBinding)
        }
        val executorView = ExecutorView(automaton.executor, view)
        add(executorView)
        executorView.apply {
            prefWidthProperty().bind(this@AutomatonView.widthProperty())
            layoutYProperty().bind(this@AutomatonView.heightProperty() - heightProperty())
        }
        label {
            layoutXProperty().bind(this@AutomatonView.widthProperty() - widthProperty() - 10.0)
            layoutYProperty().bind(this@AutomatonView.heightProperty() - executorView.heightProperty() - heightProperty())
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
}
