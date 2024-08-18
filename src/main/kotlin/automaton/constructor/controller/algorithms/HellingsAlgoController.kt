package automaton.constructor.controller.algorithms

import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.element.*
import automaton.constructor.utils.I18N
import automaton.constructor.utils.doNextIterationOfHellingsAlgo
import automaton.constructor.view.algorithms.CFGView
import automaton.constructor.view.algorithms.HellingsAlgoExecutionView
import automaton.constructor.view.algorithms.HellingsAlgoGrammarView
import javafx.beans.property.SimpleBooleanProperty
import javafx.collections.ObservableList
import tornadofx.*

class HellingsTransition(
    val nonterminal: Nonterminal,
    val source: AutomatonVertex,
    val target: AutomatonVertex,
    var isNew: SimpleBooleanProperty
) {
    override fun toString(): String {
        return "${nonterminal.value}, ${source.name}, ${target.name}"
    }
}

class HellingsAlgoController(
    private val openedAutomaton: Automaton
): Controller() {
    lateinit var grammar: ContextFreeGrammar

    fun getGrammar() {
        find<HellingsAlgoGrammarView>(mapOf(HellingsAlgoGrammarView::controller to this)).apply {
            title = I18N.messages.getString("HellingsAlgorithm.Grammar.Title")
        }.openWindow()
    }

    fun prepareForExecution(
        currentTransitions: ObservableList<HellingsTransition>,
        allTransitions: ObservableList<HellingsTransition>
        ) {
        openedAutomaton.transitions.forEach { transition ->
            val production = grammar.productions.find {
                it.rightSide.size == 1 && it.rightSide[0] is Terminal && it.rightSide[0].getSymbol() == transition.propetiesText
            }
            if (production != null) {
                val newHellingsTransition = HellingsTransition(production.leftSide, transition.source,
                    transition.target, SimpleBooleanProperty(false)
                )
                currentTransitions.add(newHellingsTransition)
                allTransitions.add(newHellingsTransition)
            }
        }
    }

    fun execute() {
        val currentTransitions = observableListOf<HellingsTransition>()
        val allTransitions = observableListOf<HellingsTransition>()
        prepareForExecution(currentTransitions, allTransitions)

        val hellingsAlgoExecutionWindow = find<HellingsAlgoExecutionView>(mapOf(
            HellingsAlgoExecutionView::currentTransitions to currentTransitions,
            HellingsAlgoExecutionView::allTransitions to allTransitions
        )).apply { title = I18N.messages.getString("HellingsAlgorithm.Execution.Title") }
        hellingsAlgoExecutionWindow.openWindow()
        find<CFGView>(mapOf(CFGView::grammar to grammar)).apply {
            title = I18N.messages.getString("CFGView.Title")
        }.openWindow()

        hellingsAlgoExecutionWindow.nextIterationButton.action {
            doNextIterationOfHellingsAlgo(currentTransitions, allTransitions, grammar)
            if (currentTransitions.isEmpty()) {
                hellingsAlgoExecutionWindow.nextIterationButton.isVisible = false
            }
        }
    }
}