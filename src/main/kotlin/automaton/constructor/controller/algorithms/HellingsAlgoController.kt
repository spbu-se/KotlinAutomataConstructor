package automaton.constructor.controller.algorithms

import automaton.constructor.controller.FileController
import automaton.constructor.controller.LayoutController
import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.automaton.FiniteAutomaton
import automaton.constructor.model.element.*
import automaton.constructor.utils.I18N
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

        find<CFGView>(mapOf(CFGView::grammar to grammar)).apply{
            title = I18N.messages.getString("CFGView.Title")
        }.openWindow()
        val hellingsAlgoExecutionWindow = find<HellingsAlgoExecutionView>(mapOf(
            HellingsAlgoExecutionView::m to m,
            HellingsAlgoExecutionView::r to r
        )).apply { title = I18N.messages.getString("HellingsAlgorithm.Execution.Title") }
        hellingsAlgoExecutionWindow.openWindow()

        hellingsAlgoExecutionWindow.nextIterationButton.action {
            if (m.isEmpty()) {
                hellingsAlgoExecutionWindow.close()
                return@action
            }
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
                                mTransition.target, SimpleBooleanProperty(true))
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
                                rTransition.target, SimpleBooleanProperty(true))
                            m.add(newTransition)
                            rToAdd.add(newTransition)
                        }
                    }
                }
            } while (rToAdd.isNotEmpty())
            if (m.isEmpty()) {
                hellingsAlgoExecutionWindow.nextIterationButton.text = I18N.messages.getString(
                    "HellingsAlgorithm.Execution.Close")
            }
        }
    }
}