package automaton.constructor.view.algorithms

import automaton.constructor.controller.algorithms.HellingsTransition
import automaton.constructor.model.element.AutomatonVertex
import automaton.constructor.model.element.Nonterminal
import javafx.collections.ObservableList
import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import tornadofx.*
import java.awt.Color

class HellingsTransitionCell: ListCell<HellingsTransition>() {
    override fun updateItem(item: HellingsTransition?, empty: Boolean) {
        super.updateItem(item, empty)
        graphic = if (item != null) {
            if (item.isNew) {
                this.style = "-fx-background-color: blue"
            }
            this.style = if (item.isNew) {
                "-fx-background-color: blue"
            } else {
                "-fx-background-color: white"
            }
            label(item.nonterminal.value + ", " + item.source.name + ", " + item.target.name)
        } else {
            null
        }
    }
}

class HellingsAlgoExecutionView: View() {
    val m: ObservableList<HellingsTransition> by param()
    val r: ObservableList<HellingsTransition> by param()
    private val mListView = ListView(m).apply { this.setCellFactory { HellingsTransitionCell() } }
    private val rListView = ListView(r).apply { this.setCellFactory { HellingsTransitionCell() } }

    override val root = vbox {
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
        button("Next iteration")
    }
}