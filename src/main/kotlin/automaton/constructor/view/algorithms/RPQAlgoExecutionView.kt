package automaton.constructor.view.algorithms

import automaton.constructor.controller.algorithms.RPQAlgoController
import automaton.constructor.controller.algorithms.RPQTransition
import automaton.constructor.utils.I18N
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import javafx.geometry.Insets
import javafx.scene.control.Button
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.control.TextField
import javafx.scene.paint.Color
import tornadofx.*

class RPQTransitionCell : ListCell<RPQTransition>() {
    private val isNew = SimpleBooleanProperty()
    override fun updateItem(item: RPQTransition?, empty: Boolean) {
        super.updateItem(item, empty)
        if (item != null) {
            isNew.bind(item.isNew)
            this.style = if (item.isNew.value) {
                "-fx-background-color: aqua;"
            } else {
                "-fx-background-color: white;"
            }
            isNew.addListener { _, _, newValue ->
                this.style = if (newValue) {
                    "-fx-background-color: aqua;"
                } else {
                    "-fx-background-color: white;"
                }
            }
            graphic = label(item.source.name + ", " + item.target.name) {
                textFill = Color.BLACK
            }
        } else {
            this.style = "-fx-background-color: white;"
            graphic = null
        }
    }
}

class RPQAlgoExecutionView : Fragment() {
    val controller: RPQAlgoController by param()
    val currentTransitions: ObservableList<RPQTransition> by param()
    val allTransitions: ObservableList<RPQTransition> by param()
    val regexProperty: SimpleStringProperty by param()
    private var regexField: TextField by singleAssign()
    private val currentTransitionsListView =
        ListView(currentTransitions).apply { this.setCellFactory { RPQTransitionCell() } }
    private val allTransitionsListView = ListView(allTransitions).apply {
        this.setCellFactory { RPQTransitionCell() }
    }
    val nextIterationButton = Button(I18N.messages.getString("RPQAlgorithm.Execution.NextIteration"))

    override val root = vbox {
        label(I18N.messages.getString("RPQAlgorithm.Execution.Description")) {
            padding = Insets(5.0, 5.0, 5.0, 5.0)
        }
        hbox(spacing = 10.0) {
            label(I18N.messages.getString("RPQAlgorithm.Regex")) {
                padding = Insets(0.0, 0.0, 0.0, 5.0)
            }
            regexField = textfield(regexProperty) {
                promptText = "a|b"
                prefWidth = 128.0
                textProperty().bindBidirectional(regexProperty)
            }
        }
        padding = Insets(5.0, 5.0, 5.0, 5.0)
        hbox {
            vbox {
                label(I18N.messages.getString("RPQAlgorithm.Execution.CurrentTransitions")) {
                    padding = Insets(0.0, 0.0, 0.0, 5.0)
                }
                add(currentTransitionsListView)
            }
            vbox {
                label(I18N.messages.getString("RPQAlgorithm.Execution.AllTransitions")) {
                    padding = Insets(0.0, 0.0, 0.0, 5.0)
                }
                add(allTransitionsListView)
            }
        }
        hbox {
            add(nextIterationButton)
            padding = Insets(5.0, 5.0, 5.0, 5.0)
        }
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
