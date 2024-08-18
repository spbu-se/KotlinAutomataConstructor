package automaton.constructor.model.algorithms

import automaton.constructor.controller.algorithms.HellingsAlgoController
import automaton.constructor.controller.algorithms.HellingsTransition
import automaton.constructor.model.TestAutomatons
import automaton.constructor.model.element.ContextFreeGrammar
import automaton.constructor.model.element.Nonterminal
import automaton.constructor.model.element.Production
import automaton.constructor.model.element.Terminal
import automaton.constructor.utils.doNextIterationOfHellingsAlgo
import org.junit.jupiter.api.Test
import tornadofx.observableListOf
import kotlin.test.assertEquals

class HellingsAlgoTests {
    @Test
    fun `test from reference course` () {
        val automaton = TestAutomatons.FROM_REFERENCE_COURSE

        val S = Nonterminal("S")
        val grammar = ContextFreeGrammar(S)
        val A = grammar.addNonterminal("A")
        val B = grammar.addNonterminal("B")
        val S1 = grammar.addNonterminal("S1")
        grammar.productions.add(Production(S, mutableListOf(A, B)))
        grammar.productions.add(Production(S, mutableListOf(A, S1)))
        grammar.productions.add(Production(S1, mutableListOf(S, B)))
        grammar.productions.add(Production(A, mutableListOf(Terminal('a'))))
        grammar.productions.add(Production(B, mutableListOf(Terminal('b'))))

        val controller = HellingsAlgoController(automaton)
        controller.grammar = grammar
        val currentTransitions = observableListOf<HellingsTransition>()
        val allTransitions = observableListOf<HellingsTransition>()
        controller.prepareForExecution(currentTransitions, allTransitions)
        while (currentTransitions.isNotEmpty()) {
            doNextIterationOfHellingsAlgo(currentTransitions, allTransitions, grammar)
        }

        val expected = mutableSetOf(
            "A, S0, S1",
            "A, S1, S2",
            "A, S2, S0",
            "B, S2, S3",
            "B, S3, S2",
            "S, S1, S3",
            "S1, S1, S2",
            "S, S0, S2",
            "S1, S0, S3",
            "S, S2, S3",
            "S1, S2, S2",
            "S, S1, S2",
            "S1, S1, S3",
            "S, S0, S3",
            "S1, S0, S2",
            "S, S2, S2",
            "S1, S2, S3"
        )
        val actual = mutableSetOf<String>()
        allTransitions.forEach { actual.add(it.toString()) }
        assertEquals(expected, actual)
    }

    @Test
    fun `test from reference course with different grammar`() {
        val automaton = TestAutomatons.FROM_REFERENCE_COURSE

        val S = Nonterminal("S")
        val grammar = ContextFreeGrammar(S)
        grammar.productions.add(Production(S, mutableListOf(Terminal('a'), S, Terminal('b'), S)))
        grammar.productions.add(Production(S, mutableListOf()))
        grammar.convertToCNF()

        val controller = HellingsAlgoController(automaton)
        controller.grammar = grammar
        val currentTransitions = observableListOf<HellingsTransition>()
        val allTransitions = observableListOf<HellingsTransition>()
        controller.prepareForExecution(currentTransitions, allTransitions)
        while (currentTransitions.isNotEmpty()) {
            doNextIterationOfHellingsAlgo(currentTransitions, allTransitions, grammar)
        }


    }
}