package automaton.constructor.view.algorithms

import automaton.constructor.controller.algorithms.HellingsTransition
import automaton.constructor.utils.I18N
import javafx.beans.property.SimpleBooleanProperty
import javafx.collections.ObservableList
import javafx.geometry.Insets
import javafx.scene.control.Button
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
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
            graphic = label(item.nonterminal.value + ", " + item.source.name + ", " + item.target.name)
        } else {
            this.style = "-fx-background-color: white;"
            graphic = null
        }
    }
}

class HellingsAlgoExecutionView: Fragment() {
    val m: ObservableList<HellingsTransition> by param()
    val r: ObservableList<HellingsTransition> by param()
    private val mListView = ListView(m).apply { this.setCellFactory { HellingsTransitionCell() } }
    private val rListView = ListView(r).apply { this.setCellFactory { HellingsTransitionCell() } }
    val nextIterationButton = Button(I18N.messages.getString("HellingsAlgorithm.Execution.NextIteration"))

    override val root = vbox {
        label (I18N.messages.getString("HellingsAlgorithm.Execution.Description")) {
            padding = Insets(5.0, 5.0, 5.0, 5.0)
        }
        hbox {
            vbox {
                label("m:") {
                    padding = Insets(0.0, 0.0, 0.0, 5.0)
                }
                add(mListView)
            }
            vbox {
                label("r:") {
                    padding = Insets(0.0, 0.0, 0.0, 5.0)
                }
                add(rListView)
            }
        }
        hbox {
            add(nextIterationButton)
            padding = Insets(5.0, 5.0, 5.0, 5.0)
        }
    }
}