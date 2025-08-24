package automaton.constructor.controller.grammar

import automaton.constructor.model.automaton.RecursiveAutomaton
import automaton.constructor.model.automaton.RecursiveAutomatonBuilder
import automaton.constructor.model.grammar.EBNFGrammar
import automaton.constructor.model.factory.RecursiveAutomatonFactory
import automaton.constructor.utils.I18N
import automaton.constructor.view.grammar.EBNFInputView
import tornadofx.Controller

class RecursiveAutomatonGrammarInputController(private val factory: RecursiveAutomatonFactory) : Controller() {
    lateinit var grammar: EBNFGrammar

    fun getGrammar() = find<EBNFInputView>(mapOf(EBNFInputView::controller to this)).apply {
        title = I18N.messages.getString("EBNF.Input.Title")
    }.openWindow()

    fun onGrammarEdited() {
        factory.grammar = grammar
        factory.lastCreatedAutomaton?.let {
            it.grammar = grammar
            RecursiveAutomatonBuilder.buildFromGrammar(it, grammar)
            runCatching { it.name = grammar.initialNonterminal.value }
        }
    }

    fun buildRecursiveAutomatonFromCFG(root: RecursiveAutomaton, grammar: EBNFGrammar) {
        root.grammar = grammar
        RecursiveAutomatonBuilder.buildFromGrammar(root, grammar)
    }
}
