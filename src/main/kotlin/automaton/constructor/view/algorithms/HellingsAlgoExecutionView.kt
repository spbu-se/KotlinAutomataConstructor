package automaton.constructor.view.algorithms

import automaton.constructor.controller.algorithms.HellingsTransition
import javafx.beans.property.SimpleBooleanProperty
import javafx.collections.ObservableList
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
    val nextIterationButton = Button("Next iteration")

    override val root = vbox {
        label ("Each triple (N, S, T) means that a word composed of symbols on the path between vertices S " +
                "and T is output in the grammar if we take N as the initial nonterminal.")
        label ("Once the algorithm finishes, the list r contains the solution.")
        hbox {
            vbox {
                label("m:")
                add(mListView)
            }
            vbox {
                label("r:")
                add(rListView)
            }
        }
        add(nextIterationButton)
        style {
            fontSize = 15.0.px
        }
    }
}