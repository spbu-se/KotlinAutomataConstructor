package automaton.constructor.view.grammar

import GrammarInputView
import automaton.constructor.controller.grammar.RecursiveAutomatonGrammarInputController
import automaton.constructor.utils.I18N
import tornadofx.error

class EBNFInputView() :
    GrammarInputView(I18N.messages.getString("EBNF.Input.Info"), I18N.messages.getString("EBNF.Input.Error")) {
    val controller: RecursiveAutomatonGrammarInputController by param()

    override fun okButtonAction() {
        if (blankFieldsCount.value > 0 || productions.isEmpty()) {
            error(errorMessage)
        } else {
            controller.grammar = grammar
            controller.buildRecursiveAutomatonFromCFG()
            close()
        }
    }
}