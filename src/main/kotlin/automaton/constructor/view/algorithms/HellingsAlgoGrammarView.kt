package automaton.constructor.view.algorithms

import GrammarInputView
import automaton.constructor.controller.algorithms.HellingsAlgoController
import automaton.constructor.model.element.CFGSymbol
import automaton.constructor.model.element.ContextFreeGrammar
import automaton.constructor.model.element.Nonterminal
import automaton.constructor.model.element.Production
import automaton.constructor.utils.I18N
import tornadofx.*


class HellingsAlgoGrammarView : GrammarInputView(
    I18N.messages.getString("HellingsAlgorithm.Grammar.Info"),
    I18N.messages.getString("HellingsAlgorithm.Grammar.Error")
) {
    val controller: HellingsAlgoController by param()

    override fun okButtonAction() {
        if (blankFieldsCount.value > 0 || productions.isEmpty()) {
            error(errorMessage)
        } else {
            controller.grammar = fixGrammar()
            controller.execute()
            close()
        }
    }

    private fun fixGrammar(): ContextFreeGrammar {
        val initialNonterminal = Nonterminal(initialNonterminalValue.value)
        val fixedGrammar = ContextFreeGrammar(initialNonterminal)
        productions.forEach { production ->
            fixedGrammar.addNonterminal(production.leftSide.cfgSymbol)
            production.rightSide.value.forEach {
                if (it.cfgSymbol is Nonterminal) {
                    fixedGrammar.addNonterminal(it.cfgSymbol as Nonterminal)
                }
            }
        }
        productions.forEach { production ->
            val newLeftSide = fixedGrammar.nonterminals.find { it.value == production.leftSide.cfgSymbol.value }!!
            val newRightSide = mutableListOf<CFGSymbol>()
            production.rightSide.value.forEach { symbol ->
                if (symbol.cfgSymbol is Nonterminal) {
                    newRightSide.add(fixedGrammar.nonterminals.find { it.value == (symbol.cfgSymbol as Nonterminal).value }!!)
                } else {
                    newRightSide.add(symbol.cfgSymbol)
                }
            }
            fixedGrammar.productions.add(Production(newLeftSide, newRightSide))
        }
        fixedGrammar.convertToCNF()
        return fixedGrammar
    }
}
