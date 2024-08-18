package automaton.constructor.model.automaton

import automaton.constructor.model.action.transition.EliminateEpsilonTransitionAction
import automaton.constructor.model.automaton.flavours.AutomatonWithInputTape
import automaton.constructor.model.automaton.flavours.AutomatonWithStacks
import automaton.constructor.model.data.PushdownAutomatonData
import automaton.constructor.model.data.createAutomaton
import automaton.constructor.model.data.getData
import automaton.constructor.model.element.*
import automaton.constructor.model.memory.StackDescriptor
import automaton.constructor.model.memory.tape.InputTapeDescriptor
import automaton.constructor.model.module.finalVertices
import automaton.constructor.model.module.initialVertices
import automaton.constructor.model.property.EPSILON_VALUE
import automaton.constructor.model.property.FormalRegex
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
            val pushedValue = transition[stacks.first().pushedValue]
            if (pushedValue != EPSILON_VALUE && pushedValue.length > 1) {
                var previousState = transition.source
                for (i in pushedValue.indices) {
                    val nextState = if (i == pushedValue.lastIndex) {
                        transition.target
                    } else
                        addState()
                    val newTransition = addTransition(previousState, nextState)
                    if (i == 0) {
                        newTransition[stacks.first().pushedValue] = pushedValue[0].toString()
                    } else {
                        newTransition[stacks.first().pushedValue] = pushedValue[i].toString()
                    }
                    previousState = nextState
                }
                removeTransition(transition)
            }
        }
    }

    private fun makeTheOnlyOneFinalState() {
        if (finalVertices.size < 2 || stacks.first().acceptsByEmptyStack) {
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
            if (transition[stacks.first().expectedChar] == EPSILON_VALUE &&
                transition[stacks.first().pushedValue] == EPSILON_VALUE) {
                val newState = addState()
                val firstTransition = addTransition(transition.source, newState)
                firstTransition[stacks.first().pushedValue] = "a"
                val secondTransition = addTransition(newState, transition.target)
                secondTransition[stacks.first().expectedChar] = 'a'
                removeTransition(transition)
            }
            if (transition[stacks.first().expectedChar] != EPSILON_VALUE &&
                transition[stacks.first().pushedValue] != EPSILON_VALUE) {
                val newState = addState()
                val firstTransition = addTransition(transition.source, newState)
                firstTransition[stacks.first().pushedValue] = EPSILON_VALUE
                val secondTransition = addTransition(newState, transition.target)
                secondTransition[stacks.first().expectedChar] = EPSILON_VALUE
                removeTransition(transition)
            }
        }
    }

    private fun clearTheStackAtTheEnd() {
        val pushedSymbols = mutableSetOf<String>()
        transitions.forEach { it[stacks.first().pushedValue]?.let { symbol -> pushedSymbols.add(symbol) } }
        pushedSymbols.forEach { addTransition(finalVertices.first(), finalVertices.first()).apply {
            this[stacks.first().expectedChar] = it[0]
        } }
    }

    private fun prepareForConversionToCFG() {
        pushOnlyOneSymbol()
        makeTheOnlyOneFinalState()
        pushOrPopOnEachTransition()
        clearTheStackAtTheEnd()
    }

    private fun prepareForConversionToAcceptingByFinalState() {
        if (!stacks.first().acceptsByEmptyStack) {
            return
        }
        val newInitialState = addState()
        initialVertices.first().isInitial = false
        newInitialState.isInitial = true
        val newFinalState = addState()
        finalVertices.toList().forEach {
            it.isFinal = false
        }
        newFinalState.isFinal = true
    }

    private fun makeTheOnlyOneInitialState() {
        if (initialVertices.size < 2) {
            return
        }
        val newInitialState = addState()
        initialVertices.toList().forEach {
            addTransition(newInitialState, it)
            it.isInitial = false
        }
        newInitialState.isInitial = true
    }

    private fun startWithEmptyStack() {
        if (stacks.first().value.isEmpty()) {
            return
        }
        val newInitialState = addState()
        val oldInitialState = initialVertices.first()
        oldInitialState.isInitial = false
        val transition = addTransition(newInitialState, oldInitialState)
        transition[stacks.first().pushedValue] = stacks.first().value
    }

    fun convertToCFG(): ContextFreeGrammar {
        if (grammar != null) {
            return grammar as ContextFreeGrammar
        }
        val initialNonterminal = Nonterminal("S")
        val newGrammar = ContextFreeGrammar(initialNonterminal)
        val nonterminals = mutableListOf<MutableList<Nonterminal>>()
        var biggestNonterminal: Nonterminal? = null
        val automatonCopy = getData().createAutomaton() as PushdownAutomaton

        if (automatonCopy.initialVertices.isEmpty()) {
            grammar = newGrammar
            return newGrammar
        }
        automatonCopy.makeTheOnlyOneInitialState()
        automatonCopy.startWithEmptyStack()
        val oldInitialState = automatonCopy.initialVertices.first()
        automatonCopy.prepareForConversionToAcceptingByFinalState()
        if (automatonCopy.finalVertices.isEmpty()) {
            grammar = newGrammar
            return newGrammar
        }
        automatonCopy.prepareForConversionToCFG()
        automatonCopy.vertices.forEach { vertice1 ->
            val list = mutableListOf<Nonterminal>()
            automatonCopy.vertices.forEach { vertice2 ->
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
        automatonCopy.transitions.forEach { transition1 ->
            automatonCopy.transitions.forEach { transition2 ->
                if (transition1[stacks.first().pushedValue] != EPSILON_VALUE &&
                    transition1[stacks.first().pushedValue]!![0] == transition2[stacks.first().expectedChar]) {
                    val indexOfSource1 = automatonCopy.vertices.indexOf(transition1.source)
                    val indexOfSource2 = automatonCopy.vertices.indexOf(transition2.source)
                    val indexOfTarget1 = automatonCopy.vertices.indexOf(transition1.target)
                    val indexOfTarget2 = automatonCopy.vertices.indexOf(transition2.target)

                    val rightSideOfNewProduction = mutableListOf<CFGSymbol>()
                    val transitionTapeChar1 = transition1[inputTape.expectedChar]
                    if (transitionTapeChar1 != EPSILON_VALUE && transitionTapeChar1 is FormalRegex.Singleton) {
                        rightSideOfNewProduction.add(Terminal(transitionTapeChar1.char))
                    }
                    rightSideOfNewProduction.add(nonterminals[indexOfTarget1][indexOfSource2])
                    val transitionTapeChar2 = transition2[inputTape.expectedChar]
                    if (transitionTapeChar2 != EPSILON_VALUE && transitionTapeChar2 is FormalRegex.Singleton) {
                        rightSideOfNewProduction.add(Terminal(transitionTapeChar2.char))
                    }
                    newGrammar.productions.add(
                        Production(nonterminals[indexOfSource1][indexOfTarget2], rightSideOfNewProduction))
                }
            }
        }
        if (automatonCopy.stacks.first().acceptsByEmptyStack) {
            automatonCopy.vertices.forEach {
                if (!it.isInitial && !it.isFinal) {
                    val indexOfSource1 = automatonCopy.vertices.indexOf(automatonCopy.initialVertices.first())
                    val indexOfSource2 = automatonCopy.vertices.indexOf(it)
                    val indexOfTarget1 = automatonCopy.vertices.indexOf(oldInitialState)
                    val indexOfTarget2 = automatonCopy.vertices.indexOf(automatonCopy.finalVertices.first())
                    newGrammar.productions.add(Production(nonterminals[indexOfSource1][indexOfTarget2],
                        mutableListOf(nonterminals[indexOfTarget1][indexOfSource2])))
                }
            }
        }

        if (biggestNonterminal != null) {
            newGrammar.productions.add(Production(initialNonterminal, mutableListOf(biggestNonterminal!!)))
        }
        newGrammar.convertToCNF()
        newGrammar.removeUselessNonterminals()
        grammar = newGrammar
        return newGrammar
    }
}
