package automaton.constructor.controller.algorithms.rpq

import automaton.constructor.model.automaton.FiniteAutomaton
import automaton.constructor.model.automaton.flavours.AutomatonWithInputTape
import automaton.constructor.model.automaton.getClosure
import automaton.constructor.model.element.AutomatonVertex
import automaton.constructor.model.element.State
import automaton.constructor.model.element.Transition
import automaton.constructor.model.property.EPSILON_VALUE
import automaton.constructor.model.property.FormalRegex
import javafx.beans.property.SimpleBooleanProperty


/**
 * RPQ algorithm
 *
 * Result: P = { (v, w) | v belongs to initial graph vertices, w reachable via a path whose label is accepted by regexFA, and w does not belong to initial graph vertices }
 */
class RPQAlgo(
    private val graphFA: FiniteAutomaton, private val regexFA: FiniteAutomaton
) {
    private data class ProductState(val g: AutomatonVertex, val r: AutomatonVertex, val src: AutomatonVertex)

    private val queue = ArrayDeque<ProductState>()
    private val visited = HashSet<ProductState>()
    private val resultPairs = LinkedHashSet<Pair<AutomatonVertex, AutomatonVertex>>()

    // For UI incremental highlighting
    private val discoveredGraphEdges = LinkedHashMap<Pair<AutomatonVertex, AutomatonVertex>, RPQTransition>()
    private val allTransitions = mutableListOf<RPQTransition>()

    private val initialGraphStates by lazy { graphFA.states.filter { it.isInitial }.toSet() }

    private val regexDescriptor = (regexFA as AutomatonWithInputTape).inputTape
    private val graphDescriptor = (graphFA as AutomatonWithInputTape).inputTape

    private val regexClosureCache = mutableMapOf<State, Set<State>>()

    private var initialized = false
    private var finished = false

    fun isFinished(): Boolean = finished
    fun getAllTransitions(): List<RPQTransition> = allTransitions
    fun getResultPairs(): Set<Pair<AutomatonVertex, AutomatonVertex>> = resultPairs

    fun nextIteration(): List<RPQTransition> {
        ensureInitialized()
        if (finished) return emptyList()

        val current = queue.removeFirstOrNull() ?: run {
            finished = true
            return emptyList()
        }
        val (gState, rState, src) = current

        val newlyDiscovered = mutableListOf<RPQTransition>()

        val regexEdgeMap = HashMap<Char, Transition>()
        for (rt in regexFA.getOutgoingTransitions(rState)) {
            val c = singleChar(rt, regexDescriptor) ?: continue
            regexEdgeMap[c] = rt
        }

        for (gTr in graphFA.getOutgoingTransitions(gState)) {
            val gc = singleChar(gTr, graphDescriptor) ?: continue
            val rTr = regexEdgeMap[gc] ?: continue

            val gTarget = gTr.target
            val rTarget = rTr.target

            for (rC in epsilonClosureRegex(rTarget)) {
                val next = ProductState(gTarget, rC, src)
                val isNew = visited.add(next)
                if (isNew) queue.addLast(next)

                if (rC.isFinal && gTarget !in initialGraphStates) {
                    resultPairs.add(src to gTarget)
                }

                val edgeKey = gState to gTarget
                if (discoveredGraphEdges[edgeKey] == null) {
                    val rpqTr = RPQTransition(gState, gTarget)
                    discoveredGraphEdges[edgeKey] = rpqTr
                    newlyDiscovered.add(rpqTr)
                    allTransitions.add(rpqTr)
                }
            }
        }

        if (queue.isEmpty()) finished = true
        return newlyDiscovered
    }

    private fun ensureInitialized() {
        if (initialized) return
        initialized = true

        val initialRegexClosure = regexFA.states.filter { it.isInitial }.flatMap { epsilonClosureRegex(it) }.toSet()

        for (g0 in initialGraphStates) {
            for (r0 in initialRegexClosure) {
                val ps = ProductState(g0, r0, g0)
                if (visited.add(ps)) queue.addLast(ps)
            }
        }
        if (queue.isEmpty()) finished = true
    }

    private fun epsilonClosureRegex(v: AutomatonVertex): Set<State> = when (v) {
        is State -> regexClosureCache.getOrPut(v) {
            regexFA.getClosure(v).filterIsInstance<State>().toSet()
        }

        else -> emptySet()
    }

    private fun singleChar(
        tr: Transition, descriptor: automaton.constructor.model.memory.tape.InputTapeDescriptor
    ): Char? {
        return when (val label = tr[descriptor.expectedChar]) {
            is FormalRegex.Singleton -> label.char
            EPSILON_VALUE -> null
            else -> {
                null
            }
        }
    }
}

class RPQTransition(
    val source: AutomatonVertex,
    val target: AutomatonVertex,
    var isNew: SimpleBooleanProperty = SimpleBooleanProperty(true)
) {
    override fun toString(): String = "(${source.name}, ${target.name})"
}
