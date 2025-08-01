package automaton.constructor.controller.grammar

import automaton.constructor.model.element.ContextFreeGrammar
import automaton.constructor.utils.I18N
import automaton.constructor.view.grammar.EBNFInputView
import tornadofx.*

class RecursiveAutomatonGrammarInputController : Controller() {
    lateinit var grammar: ContextFreeGrammar

    fun getGrammar() {
        find<EBNFInputView>(mapOf(EBNFInputView::controller to this)).apply {
            title = I18N.messages.getString("EBNF.Input.Title")
        }.openWindow()

    }

    fun buildRecursiveAutomatonFromCFG() {
        TODO("Not yet implemented")
    }
}