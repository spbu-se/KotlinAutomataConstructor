package automaton.constructor.model.automaton

import automaton.constructor.model.action.transition.EliminateEpsilonTransitionAction
import automaton.constructor.model.automaton.flavours.AutomatonWithInputTape
import automaton.constructor.model.automaton.flavours.AutomatonWithStacks
import automaton.constructor.model.data.PushdownAutomatonData
import automaton.constructor.model.element.*
import automaton.constructor.model.memory.StackDescriptor
import automaton.constructor.model.memory.tape.InputTapeDescriptor
import automaton.constructor.model.module.finalVertices
import automaton.constructor.utils.I18N

/**
 * Pushdown automaton.
 *
 * It's an automaton with an [input tape][inputTape] and several [stacks] as [memory descriptors][memoryDescriptors].
 */
class PushdownAutomaton(
    override val inputTape: InputTapeDescriptor, override val stacks: List<StackDescriptor>
) : AbstractAutomaton(
    DISPLAY_NAME,
    memoryDescriptors = listOf(inputTape) + stacks,
    I18N.messages.getString("PushdownAutomaton.Deterministic"),
    I18N.messages.getString("PushdownAutomaton.Nondeterministic"),
    I18N.messages.getString("PushdownAutomaton.Untitled")
), AutomatonWithInputTape,
    AutomatonWithStacks {
    private var grammar: ContextFreeGrammar? = null
    init {
        require(stacks.isNotEmpty()) {
            "Illegal `stacks` argument when creating `PushdownAutomaton`"
        }
    }

    override val transitionActions = super.transitionActions + listOf(
        EliminateEpsilonTransitionAction(automaton = this)
    )

    override fun getTypeData() =
        PushdownAutomatonData(inputTape = inputTape.getData(), stacks = stacks.map { it.getData() })

    override fun createEmptyAutomatonOfSameType() = PushdownAutomaton(inputTape, stacks)

    companion object {
        val DISPLAY_NAME: String = I18N.messages.getString("PushdownAutomaton")
    }

    private fun pushOnlyOneSymbol() {
        transitions.toList().forEach { transition ->
            if (transition.sideEffectsText.length > 1) {
                var previousState = transition.source
                for (i in transition.sideEffectsText.indices) {
                    val nextState = if (i == transition.sideEffectsText.lastIndex) {
                        transition.target
                    } else
                        addState()
                    val newTransition = addTransition(previousState, nextState)
                    if (i == 0) {
                        newTransition.writeProperties(transition.readProperties().toMutableList().apply { this[2] =
                            transition.sideEffectsText[0].toString()
                        })
                    } else {
                        newTransition.writeProperties(newTransition.readProperties().toMutableList().apply { this[2] =
                            transition.sideEffectsText[i].toString()
                        })
                    }
                    previousState = nextState
                }
                removeTransition(transition)
            }
        }
    }

    private fun makeTheOnlyOneFinalState() {
        if (finalVertices.size < 2) {
            return
        }
        val newFinalState = addState()
        finalVertices.toList().forEach {
            addTransition(it, newFinalState)
            it.isFinal = false
        }
        newFinalState.isFinal = true
    }

    private fun pushOrPopOnEachTransition() {
        transitions.toList().forEach { transition ->
            val list = transition.readProperties().toMutableList()
            if (list[1] == "ε" && list[2] == "ε") {
                val newState = addState()
                val firstTransition = addTransition(transition.source, newState)
                firstTransition.writeProperties(firstTransition.readProperties().toMutableList().apply { this[2] = "a" })
                val secondTransition = addTransition(newState, transition.target)
                secondTransition.writeProperties(secondTransition.readProperties().toMutableList().apply { this[1] = "a" })
                removeTransition(transition)
            }
            if (list[1] != "ε" && list[2] != "ε") {
                val newState = addState()
                val firstTransition = addTransition(transition.source, newState)
                firstTransition.writeProperties(transition.readProperties().toMutableList().apply { this[2] = "ε" })
                val secondTransition = addTransition(newState, transition.target)
                secondTransition.writeProperties(transition.readProperties().toMutableList().apply { this[1] = "ε" })
                removeTransition(transition)
            }
        }
    }

    private fun clearTheStackAtTheEnd() {
        val pushedSymbols = mutableSetOf<String>()
        transitions.forEach { pushedSymbols.add(it.readProperties()[2]) }
        pushedSymbols.remove("ε")
        pushedSymbols.forEach { addTransition(finalVertices.first(), finalVertices.first()).apply {
            this.writeProperties(this.readProperties().toMutableList().apply { this[1] = it })
        } }
    }

    private fun simplify() {
        pushOnlyOneSymbol()
        makeTheOnlyOneFinalState()
        pushOrPopOnEachTransition()
        clearTheStackAtTheEnd()
    }

    fun convertToCFG(): ContextFreeGrammar {
        if (grammar != null) {
            return grammar as ContextFreeGrammar
        }
        simplify()
        val newGrammar = ContextFreeGrammar()
        val nonterminals = mutableListOf<MutableList<Nonterminal>>()
        var biggestNonterminal: Nonterminal? = null
        vertices.forEach { vertice1 ->
            val list = mutableListOf<Nonterminal>()
            vertices.forEach { vertice2 ->
                val newNonterminal = newGrammar.addNonterminal()
                list.add(newNonterminal)
                if (vertice1.isInitial && vertice2.isFinal) {
                    biggestNonterminal = newNonterminal
                }
            }
            nonterminals.add(list)
        }

        for (i in nonterminals.indices) {
            newGrammar.productions.add(Production(nonterminals[i][i], mutableListOf()))
        }
        for (i in nonterminals.indices) {
            for (j in nonterminals.indices) {
                for (k in nonterminals.indices) {
                    newGrammar.productions.add(Production(nonterminals[i][j],
                        mutableListOf(nonterminals[i][k], nonterminals[k][j])
                    ))
                }
            }
        }
        transitions.forEach { transition1 ->
            transitions.forEach { transition2 ->
                if (transition1.readProperties()[2] == transition2.readProperties()[1] &&
                    transition1.readProperties()[2] != "ε") {
                    val indexOfSource1 = vertices.indexOf(transition1.source)
                    val indexOfSource2 = vertices.indexOf(transition2.source)
                    val indexOfTarget1 = vertices.indexOf(transition1.target)
                    val indexOfTarget2 = vertices.indexOf(transition2.target)

                    val rightSideOfNewProduction = mutableListOf<CFGSymbol>()
                    if (transition1.readProperties()[0] != "ε") {
                        rightSideOfNewProduction.add(Terminal(transition1.readProperties()[0][0]))
                    }
                    rightSideOfNewProduction.add(nonterminals[indexOfTarget1][indexOfSource2])
                    if (transition2.readProperties()[0] != "ε") {
                        rightSideOfNewProduction.add(Terminal(transition2.readProperties()[0][0]))
                    }
                    newGrammar.productions.add(
                        Production(nonterminals[indexOfSource1][indexOfTarget2], rightSideOfNewProduction))
                }
            }
        }
        val initialNonterminal = Nonterminal("S")
        newGrammar.addNonterminal(initialNonterminal)
        newGrammar.initialNonterminal = initialNonterminal
        newGrammar.productions.add(Production(initialNonterminal, mutableListOf(biggestNonterminal!!)))
        newGrammar.convertToCNF()
        newGrammar.removeUselessNonterminals()
        grammar = if (newGrammar.initialNonterminal == null) {
            val emptyGrammar = ContextFreeGrammar()
            emptyGrammar.addNonterminal(initialNonterminal)
            emptyGrammar.initialNonterminal = initialNonterminal
            emptyGrammar.productions.add(Production(initialNonterminal, mutableListOf()))
            emptyGrammar
        } else {
            newGrammar
        }
        return grammar as ContextFreeGrammar
    }
}
