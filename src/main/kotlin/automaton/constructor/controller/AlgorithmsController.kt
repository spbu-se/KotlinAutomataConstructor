package automaton.constructor.controller

import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.automaton.PushdownAutomaton
import automaton.constructor.model.element.Nonterminal
import automaton.constructor.view.algorithms.ConversionToCFGView
import javafx.scene.layout.HBox
import javafx.scene.text.Font
import tornadofx.Controller
import tornadofx.label

class AlgorithmsController(val openedAutomaton: Automaton): Controller() {
    fun convertToCFG() {
        if (openedAutomaton !is PushdownAutomaton) {
            return
        }
        val conversionToCFGWindow = find<ConversionToCFGView>(mapOf(
            ConversionToCFGView::grammar to openedAutomaton.convertToCFG(),
            ConversionToCFGView::controller to this
        ))
        conversionToCFGWindow.openWindow()
    }



    fun getLabelsForNonterminal(nonterminal: Nonterminal): HBox {
        return HBox().apply {
            this.label(nonterminal.value[0].toString())
            this.label(nonterminal.value.subSequence(1, nonterminal.value.length).toString()) {
                font = Font(9.0)
            }
        }
    }
}