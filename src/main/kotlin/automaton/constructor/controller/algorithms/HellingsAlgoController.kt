package automaton.constructor.controller.algorithms

import automaton.constructor.controller.FileController
import automaton.constructor.controller.LayoutController
import automaton.constructor.model.automaton.FiniteAutomaton
import automaton.constructor.model.automaton.PushdownAutomaton
import automaton.constructor.model.element.*
import automaton.constructor.view.algorithms.HellingsAlgoExecutionView
import automaton.constructor.view.algorithms.HellingsAlgoInputView
import tornadofx.Controller
import tornadofx.observableListOf

class HellingsTransition(
    val nonterminal: Nonterminal,
    val source: AutomatonVertex,
    val target: AutomatonVertex,
    var isNew: Boolean
)

class HellingsAlgoController(
    val openedAutomaton: PushdownAutomaton,
    val fileController: FileController,
    val layoutController: LayoutController
): Controller() {
    fun getInputGraph() {
        val hellingsAlgoInputWindow = find<HellingsAlgoInputView>(mapOf(
            HellingsAlgoInputView::hellingsAlgoController to this,
            HellingsAlgoInputView::fileController to fileController,
            HellingsAlgoInputView::layoutController to layoutController
        ))
        hellingsAlgoInputWindow.openWindow()
    }

    fun execute(graph: FiniteAutomaton) {
        val m = observableListOf<HellingsTransition>()
        val r = observableListOf<HellingsTransition>()
        val grammar = getTestGrammar()
        graph.transitions.forEach { transition ->
            val production = grammar.productions.find {
                it.rightSide.size == 1 && it.rightSide[0] is Terminal && it.rightSide[0].getSymbol() == transition.propetiesText
            }
            if (production != null) {
                val newHellingsTransition = HellingsTransition(production.leftSide, transition.source, transition.target, false)
                m.add(newHellingsTransition)
                r.add(newHellingsTransition)
            }
        }

        val hellingsAlgoExecutionWindow = find<HellingsAlgoExecutionView>(mapOf(
            HellingsAlgoExecutionView::m to m,
            HellingsAlgoExecutionView::r to r
        ))
        hellingsAlgoExecutionWindow.openWindow()
    }

    fun getTestGrammar(): ContextFreeGrammar {
        val grammar = ContextFreeGrammar()
        val s = Nonterminal("S").also { grammar.initialNonterminal = it }
        val a = Nonterminal("A")
        val b = Nonterminal("B")
        val s1 = Nonterminal("S1")
        grammar.addNonterminal(s)
        grammar.addNonterminal(a)
        grammar.addNonterminal(b)
        grammar.addNonterminal(s1)
        grammar.productions.addAll(listOf(
            Production(s, mutableListOf(a, b)),
            Production(s, mutableListOf(a, s1)),
            Production(s1, mutableListOf(s, b)),
            Production(a, mutableListOf(Terminal('a'))),
            Production(b, mutableListOf(Terminal('b')))
        ))
        return grammar
    }
}