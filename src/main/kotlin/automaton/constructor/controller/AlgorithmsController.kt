package automaton.constructor.controller

import automaton.constructor.controller.algorithms.ConversionToCFGController
import automaton.constructor.controller.algorithms.HellingsAlgoController
import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.automaton.PushdownAutomaton
import tornadofx.Controller

class AlgorithmsController(
    val openedAutomaton: Automaton,
    val fileController: FileController,
    val layoutController: LayoutController
): Controller() {
    fun convertToCFG() {
        if (openedAutomaton !is PushdownAutomaton || openedAutomaton.stacks.size > 1) {
            tornadofx.error("Conversion is done only for pushdown automatons with a single stack!")
            return
        }
        ConversionToCFGController(openedAutomaton).convertToCFG()
    }

    fun executeHellingsAlgo() {
        HellingsAlgoController(openedAutomaton, fileController, layoutController).getGrammar()
    }
}