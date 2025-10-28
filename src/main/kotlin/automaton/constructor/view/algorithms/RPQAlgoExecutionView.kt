package automaton.constructor.view.algorithms

import automaton.constructor.controller.algorithms.RPQAlgoController
import automaton.constructor.controller.algorithms.RPQResultPair
import automaton.constructor.controller.algorithms.rpq.RPQTransition
import automaton.constructor.utils.I18N
import javafx.beans.property.StringProperty
import javafx.collections.ObservableList
import javafx.geometry.Insets
import javafx.scene.control.Button
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.control.TextField
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import tornadofx.*

class RPQTransitionCell : ListCell<RPQTransition>() {
    override fun updateItem(item: RPQTransition?, empty: Boolean) {
        super.updateItem(item, empty)
        if (empty || item == null) {
            style = "-fx-background-color: white;"
            graphic = null
        } else {
            // Color background if transition was newly discovered this iteration
            style = if (item.isNew.value) "-fx-background-color: aqua;" else "-fx-background-color: white;"
            item.isNew.addListener { _, _, nv ->
                style = if (nv) "-fx-background-color: aqua;" else "-fx-background-color: white;"
            }
            graphic = label(item.toString()) {
                textFill = Color.BLACK
            }
        }
    }
}

class RPQResultPairCell : ListCell<RPQResultPair>() {
    override fun updateItem(item: RPQResultPair?, empty: Boolean) {
        super.updateItem(item, empty)
        if (empty || item == null) {
            style = "-fx-background-color: white;"
            graphic = null
        } else {
            graphic = label(item.toString())
        }
    }
}

class RPQAlgoExecutionView : Fragment() {
    val controller: RPQAlgoController by param()
    val currentTransitions: ObservableList<RPQTransition> by param()
    val allTransitions: ObservableList<RPQTransition> by param()
    val resultPairs: ObservableList<RPQResultPair> by param()
    val regexProperty: StringProperty by param()

    private var regexField: TextField by singleAssign()

    val nextIterationButton = Button(I18N.messages.getString("RPQAlgorithm.Execution.NextIteration"))
    val runToEndButton = Button(I18N.messages.getString("RPQAlgorithm.Execution.RunToEnd"))
    val resetButton = Button(I18N.messages.getString("RPQAlgorithm.Execution.Reset"))

    private val currentTransitionsListView =
        ListView(currentTransitions).apply { setCellFactory { RPQTransitionCell() } }

    private val allTransitionsListView = ListView(allTransitions).apply { setCellFactory { RPQTransitionCell() } }

    private val resultPairsListView = ListView(resultPairs).apply { setCellFactory { RPQResultPairCell() } }

    override val root = vbox {
        padding = Insets(8.0)
        spacing = 8.0

        label(I18N.messages.getString("RPQAlgorithm.Execution.Info")) {
            padding = Insets(4.0)
        }
        text(I18N.messages.getString("RPQAlgorithm.Execution.Description")) {
            padding = Insets(4.0)
        }

        hbox(spacing = 10.0) {
            alignment = javafx.geometry.Pos.CENTER_LEFT
            label(I18N.messages.getString("RPQAlgorithm.Regex")) {
                padding = Insets(0.0, 0.0, 0.0, 4.0)
            }
            regexField = textfield(regexProperty) {
                promptText = "a|(ab)*"
                prefWidth = 160.0
                textProperty().bindBidirectional(regexProperty)
            }
        }

        hbox(spacing = 12.0) {
            vbox {
                label(I18N.messages.getString("RPQAlgorithm.Execution.CurrentTransitions"))
                this += currentTransitionsListView
                vgrow = Priority.ALWAYS
            }
            vbox {
                label(I18N.messages.getString("RPQAlgorithm.Execution.AllTransitions"))
                this += allTransitionsListView
                vgrow = Priority.ALWAYS
            }
            vbox {
                label(I18N.messages.getString("RPQAlgorithm.Execution.ResultPairs"))
                this += resultPairsListView
                vgrow = Priority.ALWAYS
            }
        }

        hbox(spacing = 10.0) {
            add(nextIterationButton)
            add(runToEndButton)
            add(resetButton)
            padding = Insets(4.0)
        }

        separator()
        prefWidth = 900.0
        prefHeight = 520.0
    }

    fun lockRegexInput() {
        regexField.isEditable = false
        regexField.isDisable = true
    }

    fun unlockRegexInput() {
        regexField.isEditable = true
        regexField.isDisable = false
    }
}
