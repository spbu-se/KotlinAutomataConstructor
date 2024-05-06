package automaton.constructor.controller

import automaton.constructor.controller.algorithms.ConversionToCFGController
import automaton.constructor.controller.algorithms.HellingsAlgoController
import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.automaton.PushdownAutomaton
import automaton.constructor.model.element.Nonterminal
import automaton.constructor.view.algorithms.ConversionToCFGView
import javafx.scene.layout.HBox
import javafx.scene.text.Font
import tornadofx.Controller
import tornadofx.label

class AlgorithmsController(
    val openedAutomaton: Automaton,
    val fileController: FileController,
    val layoutController: LayoutController
): Controller() {
    fun convertToCFG() {
        if (openedAutomaton !is PushdownAutomaton) {
            return
        }
        ConversionToCFGController(openedAutomaton).convertToCFG()
    }

    fun executeHellingsAlgo() {
        if (openedAutomaton !is PushdownAutomaton) {
            return
        }
        HellingsAlgoController(openedAutomaton, fileController, layoutController).getInputGraph()
    }
}