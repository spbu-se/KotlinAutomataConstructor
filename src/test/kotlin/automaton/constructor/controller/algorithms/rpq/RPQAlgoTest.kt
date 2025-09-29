package automaton.constructor.controller.algorithms.rpq

import automaton.constructor.model.automaton.FiniteAutomaton
import automaton.constructor.model.element.State
import automaton.constructor.model.memory.tape.InputTapeDescriptor
import automaton.constructor.model.property.EPSILON_VALUE
import automaton.constructor.model.property.FormalRegex
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * RPQAlgo tests
 */
class RPQAlgoTest {
    private fun newFA(): FiniteAutomaton = FiniteAutomaton(InputTapeDescriptor())

    private fun addState(
        fa: FiniteAutomaton, name: String, initial: Boolean = false, final: Boolean = false
    ): State = fa.addState(name).apply {
        isInitial = initial
        isFinal = final
    }

    private fun addTransition(fa: FiniteAutomaton, from: State, to: State, c: Char) {
        fa.addTransition(from, to)[fa.inputTape.expectedChar] = FormalRegex.Singleton(c)
    }

    private fun addEpsilon(fa: FiniteAutomaton, from: State, to: State) {
        fa.addTransition(from, to)[fa.inputTape.expectedChar] = EPSILON_VALUE
    }

    /**
     * Builds a linear regex automaton for an exact word.
     */
    private fun regexFA(word: String): FiniteAutomaton {
        val fa = newFA()
        var prev = addState(fa, "r0", initial = true, final = word.isEmpty())
        word.forEachIndexed { i, ch ->
            val next = addState(fa, "r${i + 1}")
            addTransition(fa, prev, next, ch)
            prev = next
        }
        prev.isFinal = true
        return fa
    }

    /**
     * Manually builds a regex automaton with alternation
     */
    private fun alternationRegexFA(vararg symbols: Char): FiniteAutomaton {
        val fa = newFA()
        val r0 = addState(fa, "r0", initial = true)
        val r1 = addState(fa, "r1", final = true)
        symbols.forEach { addTransition(fa, r0, r1, it) }
        return fa
    }

    /**
     * Graph with a single a-labeled edge g0 -> g1 (g0 initial).
     */
    private fun singleEdgeGraph(c: Char): Triple<FiniteAutomaton, State, State> {
        val fa = newFA()
        val g0 = addState(fa, "g0", initial = true)
        val g1 = addState(fa, "g1")
        addTransition(fa, g0, g1, c)
        return Triple(fa, g0, g1)
    }

    /**
     * Graph linear chain for given label sequence.
     */
    private fun chainGraph(sequence: String): Triple<FiniteAutomaton, State, State> {
        val fa = newFA()
        var prev = addState(fa, "g0", initial = true)
        sequence.forEachIndexed { i, ch ->
            val next = addState(fa, "g${i + 1}")
            addTransition(fa, prev, next, ch)
            prev = next
        }
        return Triple(fa, fa.states.first { it.isInitial }, prev)
    }

    /**
     * Run algorithm to completion and return it.
     */
    private fun run(graph: FiniteAutomaton, regex: FiniteAutomaton): RPQAlgo {
        val algo = RPQAlgo(graph, regex)
        while (!algo.isFinished()) algo.nextIteration()
        return algo
    }

    @Test
    fun `single symbol graph and matching regex produce one pair`() {
        val (graph, g0, g1) = singleEdgeGraph('a')
        val algo = run(graph, regexFA("a"))
        assertEquals(setOf(g0 to g1), algo.getResultPairs())
    }

    @Test
    fun `single symbol graph and non matching regex produce empty set`() {
        val (graph, _, _) = singleEdgeGraph('a')
        val algo = run(graph, regexFA("b"))
        assertTrue(algo.getResultPairs().isEmpty())
    }

    @Test
    fun `multi symbol chain accepted only at final vertex`() {
        val (graph, g0, gEnd) = chainGraph("abc")
        val algo = run(graph, regexFA("abc"))
        assertEquals(setOf(g0 to gEnd), algo.getResultPairs())
    }

    @Test
    fun `multi symbol chain mismatch produces empty result`() {
        val (graph, _, _) = chainGraph("abc")
        val algo = run(graph, regexFA("abd"))
        assertTrue(algo.getResultPairs().isEmpty())
    }

    @Test
    fun `regex with leading epsilon accepts path after first symbol`() {
        // Graph: g0 -x-> g1
        val graph = newFA()
        val g0 = addState(graph, "g0", initial = true)
        val g1 = addState(graph, "g1")
        addTransition(graph, g0, g1, 'x')

        // Regex: r0 -ε-> r1 -x-> r2(final)
        val regex = newFA()
        val r0 = addState(regex, "r0", initial = true)
        val r1 = addState(regex, "r1")
        val r2 = addState(regex, "r2", final = true)
        addEpsilon(regex, r0, r1)
        addTransition(regex, r1, r2, 'x')

        val algo = run(graph, regex)
        assertEquals(setOf(g0 to g1), algo.getResultPairs())
    }

    @Test
    fun `alternative regex with one matching symbol produces a pair`() {
        val (graph, g0, g1) = singleEdgeGraph('b')
        val algo = run(graph, alternationRegexFA('a', 'b', 'c'))
        assertEquals(setOf(g0 to g1), algo.getResultPairs())
    }

    @Test
    fun `alternative regex with no matching symbol yields empty set`() {
        val (graph, _, _) = singleEdgeGraph('z')
        val algo = run(graph, alternationRegexFA('a', 'b', 'c'))
        assertTrue(algo.getResultPairs().isEmpty())
    }

    @Test
    fun `incremental iteration discovers edges once and produces final pair`() {
        val (graph, g0, g2) = chainGraph("ab")
        val regex = regexFA("ab")
        val algo = RPQAlgo(graph, regex)

        val step1 = algo.nextIteration()
        val step2 = algo.nextIteration()
        val step3 = algo.nextIteration()

        val allSteps = step1 + step2 + step3
        val uniqueByEndpoints = allSteps.map { it.source to it.target }.toSet()
        assertEquals(uniqueByEndpoints.size, allSteps.size, "Duplicate edge discovery detected")

        while (!algo.isFinished()) algo.nextIteration()
        assertEquals(setOf(g0 to g2), algo.getResultPairs())
    }

    @Test
    fun `longer chain terminates and produces final pair`() {
        val (graph, g0, gEnd) = chainGraph("abcd")
        val algo = RPQAlgo(graph, regexFA("abcd"))
        while (!algo.isFinished()) algo.nextIteration()
        assertEquals(setOf(g0 to gEnd), algo.getResultPairs())
    }

    @Test
    fun `graph with no transitions produces empty result`() {
        val graph = newFA().apply { addState(this, "g0", initial = true) }
        val algo = run(graph, regexFA("a"))
        assertTrue(algo.getResultPairs().isEmpty())
        assertTrue(algo.getAllTransitions().isEmpty())
    }

    @Test
    fun `epsilon only regex does not produce pair because target would be initial vertex`() {
        val graph = newFA().apply { addState(this, "g0", initial = true) }
        val algo = run(graph, regexFA(""))
        assertTrue(algo.getResultPairs().isEmpty())
    }

    @Test
    fun `multiple initial sources produce distinct pairs`() {
        val graph = newFA()
        val g0 = addState(graph, "g0", initial = true)
        val g1 = addState(graph, "g1", initial = true)
        val g0a = addState(graph, "g0a")
        val g1a = addState(graph, "g1a")
        addTransition(graph, g0, g0a, 'a')
        addTransition(graph, g1, g1a, 'a')

        val algo = run(graph, regexFA("a"))
        assertEquals(setOf(g0 to g0a, g1 to g1a), algo.getResultPairs())
    }

    @Test
    fun `graph with two cycles and regex b(star)ab produce single result pair`() {/*
         Source: https://github.com/FormalLanguageConstrainedPathQuerying/FormalLanguageConstrainedReachability-LectureNotes/blob/dev/tex/RPQ.tex#L29-L505
         Graph:
             g0 -(a)-> g1 -(b)-> g2 -(a)-> g0
             g0 -(b)-> g3 -(b)-> g0
         Regex:  b* a b
        */
        val graph = newFA()
        val g0 = addState(graph, "g0", initial = true)
        val g1 = addState(graph, "g1")
        val g2 = addState(graph, "g2")
        val g3 = addState(graph, "g3")
        addTransition(graph, g0, g1, 'a')
        addTransition(graph, g1, g2, 'b')
        addTransition(graph, g2, g0, 'a')
        addTransition(graph, g0, g3, 'b')
        addTransition(graph, g3, g0, 'b')

        val regex = newFA()
        val r0 = addState(regex, "r0", initial = true) // loop on b
        val r1 = addState(regex, "r1")
        val r2 = addState(regex, "r2", final = true)
        addTransition(regex, r0, r0, 'b')
        addTransition(regex, r0, r1, 'a')
        addTransition(regex, r1, r2, 'b')

        val algo = run(graph, regex)
        assertEquals(setOf(g0 to g2), algo.getResultPairs())
    }
}