package automaton.constructor.view.algorithms

import automaton.constructor.controller.algorithms.HellingsTransition
import automaton.constructor.utils.I18N
import javafx.beans.property.SimpleBooleanProperty
import javafx.collections.ObservableList
import javafx.geometry.Insets
import javafx.scene.control.Button
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.paint.Color
import tornadofx.*

class HellingsTransitionCell: ListCell<HellingsTransition>() {
    private val isNew = SimpleBooleanProperty()
    override fun updateItem(item: HellingsTransition?, empty: Boolean) {
        super.updateItem(item, empty)
        if (item != null) {
            isNew.bind(item.isNew)
            this.style = if (item.isNew.value) {
                "-fx-background-color: aqua;"
            } else {
                "-fx-background-color: white;"
            }
            isNew.addListener(ChangeListener { _, _, newValue ->
                this.style = if (newValue) {
                    "-fx-background-color: aqua;"
                } else {
                    "-fx-background-color: white;"
                }
            })
            graphic = label(item.nonterminal.value + ", " + item.source.name + ", " + item.target.name) {
                textFill = Color.BLACK
            }
        } else {
            this.style = "-fx-background-color: white;"
            graphic = null
        }
    }
}

class HellingsAlgoExecutionView: Fragment() {
    val currentTransitions: ObservableList<HellingsTransition> by param()
    val allTransitions: ObservableList<HellingsTransition> by param()
    private val currentTransitionsListView = ListView(currentTransitions).apply { this.setCellFactory { HellingsTransitionCell() } }
    private val allTransitionsListView = ListView(allTransitions).apply { this.setCellFactory { HellingsTransitionCell() } }
    val nextIterationButton = Button(I18N.messages.getString("HellingsAlgorithm.Execution.NextIteration"))

    override val root = vbox {
        label (I18N.messages.getString("HellingsAlgorithm.Execution.Description")) {
            padding = Insets(5.0, 5.0, 5.0, 5.0)
        }
        hbox {
            vbox {
                label(I18N.messages.getString("HellingsAlgorithm.Execution.CurrentTransitions")) {
                    padding = Insets(0.0, 0.0, 0.0, 5.0)
                }
                add(currentTransitionsListView)
            }
            vbox {
                label(I18N.messages.getString("HellingsAlgorithm.Execution.AllTransitions")) {
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
}