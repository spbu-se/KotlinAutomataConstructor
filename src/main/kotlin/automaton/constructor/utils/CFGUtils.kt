package automaton.constructor.utils

import automaton.constructor.model.element.Nonterminal
import javafx.scene.layout.HBox
import javafx.scene.text.Font
import tornadofx.label

fun getLabelsForNonterminal(nonterminal: Nonterminal): HBox {
    return HBox().apply {
        this.label(nonterminal.value[0].toString())
        this.label(nonterminal.value.subSequence(1, nonterminal.value.length).toString()) {
            font = Font(9.0)
        }
    }
}