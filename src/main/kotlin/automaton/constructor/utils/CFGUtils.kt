package automaton.constructor.utils

import automaton.constructor.controller.algorithms.HellingsTransition
import automaton.constructor.model.element.ContextFreeGrammar
import javafx.collections.ObservableList

fun doNextIterationOfHellingsAlgo(
    currentTransitions: ObservableList<HellingsTransition>,
    allTransitions: ObservableList<HellingsTransition>,
    grammar: ContextFreeGrammar
) {
    currentTransitions.forEach { it.isNew.set(false) }
    allTransitions.forEach { it.isNew.set(false) }
    if (currentTransitions.isEmpty()) {
        return
    }
    val mTransition = currentTransitions.removeFirst()
    val rToAdd = mutableListOf<HellingsTransition>()
    do {
        allTransitions.addAll(rToAdd)
        rToAdd.clear()
        allTransitions.filter {
            it.target == mTransition.source
        }.forEach { rTransition ->
            grammar.productions.filter {
                it.rightSide == mutableListOf(rTransition.nonterminal, mTransition.nonterminal)
            }.forEach { production ->
                if (allTransitions.none { it.isEqual(HellingsTransition(production.leftSide, rTransition.source, mTransition.target)) } &&
                    rToAdd.none { it.isEqual(HellingsTransition(production.leftSide, rTransition.source, mTransition.target)) }) {
                    val newTransition = HellingsTransition(production.leftSide, rTransition.source, mTransition.target)
                    currentTransitions.add(newTransition)
                    rToAdd.add(newTransition)
                }
            }
        }
    } while (rToAdd.isNotEmpty())
    do {
        allTransitions.addAll(rToAdd)
        rToAdd.clear()
        allTransitions.filter {
            it.source == mTransition.target
        }.forEach { rTransition ->
            grammar.productions.filter {
                it.rightSide == mutableListOf(mTransition.nonterminal, rTransition.nonterminal)
            }.forEach { production ->
                if (allTransitions.none { it.isEqual(HellingsTransition(production.leftSide, mTransition.source, rTransition.target)) } &&
                    rToAdd.none { it.isEqual(HellingsTransition(production.leftSide, mTransition.source, rTransition.target)) }) {
                    val newTransition = HellingsTransition(production.leftSide, mTransition.source, rTransition.target)
                    currentTransitions.add(newTransition)
                    rToAdd.add(newTransition)
                }
            }
        }
    } while (rToAdd.isNotEmpty())
}