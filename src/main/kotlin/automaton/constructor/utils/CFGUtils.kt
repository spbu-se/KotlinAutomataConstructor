package automaton.constructor.utils

import automaton.constructor.controller.algorithms.HellingsTransition
import automaton.constructor.model.element.ContextFreeGrammar
import automaton.constructor.model.element.Nonterminal
import javafx.beans.property.SimpleBooleanProperty
import javafx.collections.ObservableList
import javafx.scene.layout.HBox
import javafx.scene.text.Font
import tornadofx.label

fun getLabelsForNonterminal(nonterminal: Nonterminal): HBox {
    return HBox().apply {
        this.label(nonterminal.value[0].toString())
        this.label(nonterminal.value.subSequence(1, nonterminal.value.length).toString()) {
            font = Font(9.0)
        }
    }
}

fun doNextIterationOfHellingsAlgo(
    m: ObservableList<HellingsTransition>,
    r: ObservableList<HellingsTransition>,
    grammar: ContextFreeGrammar
) {
    m.forEach { it.isNew.set(false) }
    r.forEach { it.isNew.set(false) }
    val mTransition = m.removeFirst()
    val rToAdd = mutableListOf<HellingsTransition>()
    do {
        r.addAll(rToAdd)
        rToAdd.clear()
        r.filter {
            it.target == mTransition.source
        }.forEach { rTransition ->
            grammar.productions.filter {
                it.rightSide == mutableListOf(rTransition.nonterminal, mTransition.nonterminal)
            }.forEach { production ->
                if (r.none { it.nonterminal == production.leftSide && it.source == rTransition.source && it.target == mTransition.target } &&
                    rToAdd.none { it.nonterminal == production.leftSide && it.source == rTransition.source && it.target == mTransition.target }) {
                    val newTransition = HellingsTransition(production.leftSide, rTransition.source,
                        mTransition.target, SimpleBooleanProperty(true)
                    )
                    m.add(newTransition)
                    rToAdd.add(newTransition)
                }
            }
        }
    } while (rToAdd.isNotEmpty())
    do {
        r.addAll(rToAdd)
        rToAdd.clear()
        r.filter {
            it.source == mTransition.target
        }.forEach { rTransition ->
            grammar.productions.filter {
                it.rightSide == mutableListOf(mTransition.nonterminal, rTransition.nonterminal)
            }.forEach { production ->
                if (r.none { it.nonterminal == production.leftSide && it.source == mTransition.source && it.target == rTransition.target } &&
                    rToAdd.none { it.nonterminal == production.leftSide && it.source == mTransition.source && it.target == rTransition.target }) {
                    val newTransition = HellingsTransition(production.leftSide, mTransition.source,
                        rTransition.target, SimpleBooleanProperty(true)
                    )
                    m.add(newTransition)
                    rToAdd.add(newTransition)
                }
            }
        }
    } while (rToAdd.isNotEmpty())
}