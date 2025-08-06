package automaton.constructor.controller.grammar

import automaton.constructor.model.automaton.RecursiveAutomaton
import automaton.constructor.model.element.EBNFGrammar
import automaton.constructor.model.factory.RecursiveAutomatonFactory
import automaton.constructor.utils.I18N
import automaton.constructor.view.grammar.EBNFInputView
import tornadofx.Controller

class RecursiveAutomatonGrammarInputController(private val factory: RecursiveAutomatonFactory) : Controller() {
    lateinit var grammar: EBNFGrammar

    fun getGrammar() {
        find<EBNFInputView>(mapOf(EBNFInputView::controller to this)).apply {
            title = I18N.messages.getString("EBNF.Input.Title")
        }.openWindow()
    }

    fun onGrammarEdited() {
        factory.grammar = grammar
    }

    fun buildRecursiveAutomatonFromCFG(automaton: RecursiveAutomaton, grammar: EBNFGrammar) {
//        TODO("Not yet implemented")
        grammar.productions.forEach { production -> println("${production.leftSide.value} -> ${production.rightSide}") }
        return
    }
}