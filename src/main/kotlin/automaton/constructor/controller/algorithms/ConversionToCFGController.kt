package automaton.constructor.controller.algorithms

import automaton.constructor.model.automaton.PushdownAutomaton
import automaton.constructor.model.data.createAutomaton
import automaton.constructor.model.data.getData
import automaton.constructor.view.algorithms.CFGView
import tornadofx.Controller

class ConversionToCFGController(private val openedAutomaton: PushdownAutomaton): Controller() {
    fun convertToCFG() {
        val automatonCopy = openedAutomaton.getData().createAutomaton() as PushdownAutomaton
        val conversionToCFGWindow = find<CFGView>(mapOf(
            CFGView::grammar to automatonCopy.convertToCFG()
        ))
        conversionToCFGWindow.openWindow()
    }
}