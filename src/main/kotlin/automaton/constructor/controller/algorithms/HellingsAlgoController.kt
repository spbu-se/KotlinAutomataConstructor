package automaton.constructor.controller.algorithms

import automaton.constructor.controller.FileController
import automaton.constructor.controller.LayoutController
import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.automaton.FiniteAutomaton
import automaton.constructor.model.element.*
import automaton.constructor.utils.I18N
import automaton.constructor.utils.doNextIterationOfHellingsAlgo
import automaton.constructor.view.algorithms.CFGView
import automaton.constructor.view.algorithms.HellingsAlgoExecutionView
import automaton.constructor.view.algorithms.HellingsAlgoGrammarView
import automaton.constructor.view.algorithms.HellingsAlgoGraphView
import javafx.beans.property.SimpleBooleanProperty
import tornadofx.*

class HellingsTransition(
    val nonterminal: Nonterminal,
    val source: AutomatonVertex,
    val target: AutomatonVertex,
    var isNew: SimpleBooleanProperty
)

class HellingsAlgoController(
    val openedAutomaton: Automaton,
    val fileController: FileController,
    val layoutController: LayoutController
): Controller() {
    lateinit var grammar: ContextFreeGrammar

    fun getGrammar() {
        find<HellingsAlgoGrammarView>(mapOf(HellingsAlgoGrammarView::controller to this)).apply {
            title = I18N.messages.getString("HellingsAlgorithm.Grammar.Title")
        }.openWindow()
    }

    fun getInputGraph() {
        find<HellingsAlgoGraphView>(mapOf(
            HellingsAlgoGraphView::hellingsAlgoController to this,
            HellingsAlgoGraphView::fileController to fileController,
            HellingsAlgoGraphView::layoutController to layoutController
        )).apply {
            title = I18N.messages.getString("HellingsAlgorithm.Graph.Title")
        }.openWindow()
    }

    fun execute(graph: FiniteAutomaton) {
        val m = observableListOf<HellingsTransition>()
        val r = observableListOf<HellingsTransition>()
        graph.transitions.forEach { transition ->
            val production = grammar.productions.find {
                it.rightSide.size == 1 && it.rightSide[0] is Terminal && it.rightSide[0].getSymbol() == transition.propetiesText
            }
            if (production != null) {
                val newHellingsTransition = HellingsTransition(production.leftSide, transition.source,
                    transition.target, SimpleBooleanProperty(false)
                )
                m.add(newHellingsTransition)
                r.add(newHellingsTransition)
            }
        }

        val hellingsAlgoExecutionWindow = find<HellingsAlgoExecutionView>(mapOf(
            HellingsAlgoExecutionView::m to m,
            HellingsAlgoExecutionView::r to r
        )).apply { title = I18N.messages.getString("HellingsAlgorithm.Execution.Title") }
        hellingsAlgoExecutionWindow.openWindow()
        find<CFGView>(mapOf(CFGView::grammar to grammar)).apply{
            title = I18N.messages.getString("CFGView.Title")
        }.openWindow()

        hellingsAlgoExecutionWindow.nextIterationButton.action {
            doNextIterationOfHellingsAlgo(m, r, grammar)
            if (m.isEmpty()) {
                hellingsAlgoExecutionWindow.nextIterationButton.isVisible = false
            }
        }
    }
}