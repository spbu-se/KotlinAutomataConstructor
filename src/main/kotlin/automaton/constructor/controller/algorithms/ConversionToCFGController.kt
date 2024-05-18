package automaton.constructor.controller.algorithms

import automaton.constructor.model.automaton.PushdownAutomaton
import automaton.constructor.model.data.createAutomaton
import automaton.constructor.model.data.getData
import automaton.constructor.model.element.Nonterminal
import automaton.constructor.view.algorithms.ConversionToCFGView
import javafx.scene.layout.HBox
import javafx.scene.text.Font
import tornadofx.Controller
import tornadofx.label

class ConversionToCFGController(private val openedAutomaton: PushdownAutomaton): Controller() {
    fun convertToCFG() {
        val automatonCopy = openedAutomaton.getData().createAutomaton() as PushdownAutomaton
        val conversionToCFGWindow = find<ConversionToCFGView>(mapOf(
            ConversionToCFGView::grammar to automatonCopy.convertToCFG(),
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