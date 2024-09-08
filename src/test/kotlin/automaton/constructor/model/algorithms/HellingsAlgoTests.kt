package automaton.constructor.model.algorithms

import automaton.constructor.controller.algorithms.HellingsAlgoController
import automaton.constructor.controller.algorithms.HellingsTransition
import automaton.constructor.model.TestAutomatons
import automaton.constructor.model.automaton.FiniteAutomaton
import automaton.constructor.model.element.ContextFreeGrammar
import automaton.constructor.model.element.Nonterminal
import automaton.constructor.model.element.Production
import automaton.constructor.model.element.Terminal
import automaton.constructor.utils.doNextIterationOfHellingsAlgo
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import tornadofx.observableListOf
import kotlin.test.assertEquals

class HellingsAlgoTests {
    fun execute(automaton: FiniteAutomaton, grammar: ContextFreeGrammar): List<HellingsTransition> {
        val controller = HellingsAlgoController(automaton)
        controller.grammar = grammar
        val currentTransitions = observableListOf<HellingsTransition>()
        val allTransitions = observableListOf<HellingsTransition>()
        controller.prepareForExecution(currentTransitions, allTransitions)
        do {
            doNextIterationOfHellingsAlgo(currentTransitions, allTransitions, grammar)
        } while(currentTransitions.isNotEmpty())
        return allTransitions
    }

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
        execute(automaton, grammar).forEach { actual.add(it.toString()) }
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

        val expected = mutableSetOf(
            "U3, S2, S3",
            "U3, S3, S2",
            "U, S1, S2",
            "U, S2, S0",
            "U, S0, S1",
            "S, S1, S3",
            "S1, S1, S3",
            "Y6, S1, S3",
            "Y, S0, S3",
            "Y7, S0, S3",
            "Y5, S0, S2",
            "S1, S0, S2",
            "S, S0, S2",
            "Y, S2, S2",
            "Y7, S2, S2",
            "Y5, S2, S3",
            "S1, S2, S3",
            "S, S2, S3",
            "Y, S1, S3",
            "Y7, S1, S3",
            "S1, S0, S3",
            "S, S0, S3",
            "Y, S1, S3",
            "Y7, S1, S3",
            "S1, S0, S3",
            "S, S0, S3",
            "Y5, S1, S2",
            "S1, S1, S2",
            "S, S1, S2",
            "Y, S2, S3",
            "Y7, S2, S3",
            "Y, S0, S2",
            "Y7, S0, S2",
            "Y5, S2, S2",
            "S1, S2, S2",
            "S, S2, S2",
            "Y5, S0, S3",
            "Y, S1, S2",
            "Y7, S1, S2",
            "Y5, S1, S3",
            "S1, S1, S1",
            "S1, S3, S3",
            "S1, S0, S0"
        )
        val actual = mutableSetOf<String>()
        execute(automaton, grammar).forEach { actual.add(it.toString()) }
        assertEquals(expected, actual)
    }

    @Test
    fun `result should be empty`() {
        val automaton = TestAutomatons.FROM_REFERENCE_COURSE

        val S = Nonterminal("S")
        val grammar = ContextFreeGrammar(S)
        val A = grammar.addNonterminal("A")
        val B = grammar.addNonterminal("B")
        val S1 = grammar.addNonterminal("S1")
        grammar.productions.add(Production(S, mutableListOf(A, B)))
        grammar.productions.add(Production(S, mutableListOf(A, S1)))
        grammar.productions.add(Production(S1, mutableListOf(S, B)))
        grammar.productions.add(Production(A, mutableListOf(Terminal('c'))))
        grammar.productions.add(Production(B, mutableListOf(Terminal('d'))))

        val expected = mutableSetOf<String>()
        val actual = mutableSetOf<String>()
        execute(automaton, grammar).forEach { actual.add(it.toString()) }
        assertEquals(expected, actual)
    }

    @Test
    fun `caacbb test`() {
        val automaton = TestAutomatons.CAACBB

        val S = Nonterminal("S")
        val grammar = ContextFreeGrammar(S)
        val A = grammar.addNonterminal("A")
        val B = grammar.addNonterminal("B")
        grammar.productions.add(Production(S, mutableListOf(A, S, B)))
        grammar.productions.add(Production(A, mutableListOf(Terminal('a'), A, S)))
        grammar.productions.add(Production(A, mutableListOf(Terminal('a'))))
        grammar.productions.add(Production(A, mutableListOf()))
        grammar.productions.add(Production(B, mutableListOf(S, Terminal('b'), S)))
        grammar.productions.add(Production(B, mutableListOf(A)))
        grammar.productions.add(Production(B, mutableListOf(Terminal('b'), Terminal('b'))))
        grammar.productions.add(Production(S, mutableListOf(Terminal('c'))))
        grammar.convertToCNF()

        assertTrue(execute(automaton, grammar).any { it.isEqual(HellingsTransition(
            grammar.initialNonterminal,
            automaton.vertices.find { it.name == "S0" }!!,
            automaton.vertices.find { it.name == "S6" }!!
        )) })
    }

    @Test
    fun `test for grammar of even palindromes recogniser`() {
        val automaton = TestAutomatons.DFA_110011

        val S = Nonterminal("S")
        val grammar = ContextFreeGrammar(S)
        val A3 = grammar.addNonterminal("A3")
        val A8 = grammar.addNonterminal("A8")
        val A12 = grammar.addNonterminal("A12")
        val A21 = grammar.addNonterminal("A21")
        val A28 = grammar.addNonterminal("A28")
        val A29 = grammar.addNonterminal("A29")
        val A63 = grammar.addNonterminal("A63")
        val A71 = grammar.addNonterminal("A71")
        val A80 = grammar.addNonterminal("A80")
        val A84 = grammar.addNonterminal("A84")
        val A94 = grammar.addNonterminal("A94")
        val A106 = grammar.addNonterminal("A106")
        val A111 = grammar.addNonterminal("A111")
        val A116 = grammar.addNonterminal("A116")
        val A120 = grammar.addNonterminal("A120")
        val A129 = grammar.addNonterminal("A129")
        val A165 = grammar.addNonterminal("A165")
        val A183 = grammar.addNonterminal("A183")
        val A201 = grammar.addNonterminal("A201")
        val A219 = grammar.addNonterminal("A219")
        val A226 = grammar.addNonterminal("A226")
        val A227 = grammar.addNonterminal("A227")
        val A262 = grammar.addNonterminal("A262")
        val A263 = grammar.addNonterminal("A263")
        val A279 = grammar.addNonterminal("A279")
        val A287 = grammar.addNonterminal("A287")
        val U = grammar.addNonterminal("U")
        val U326 = grammar.addNonterminal("U326")
        grammar.productions.add(Production(A3, mutableListOf(A8, A129)))
        grammar.productions.add(Production(A3, mutableListOf(A12, A201)))
        grammar.productions.add(Production(A21, mutableListOf(A28, A165)))
        grammar.productions.add(Production(A21, mutableListOf(A29, A183)))
        grammar.productions.add(Production(A111, mutableListOf(A116, A129)))
        grammar.productions.add(Production(A111, mutableListOf(A120, A201)))
        grammar.productions.add(Production(A219, mutableListOf(A226, A165)))
        grammar.productions.add(Production(A219, mutableListOf(A227, A183)))
        grammar.productions.add(Production(A183, mutableListOf(A21, U326)))
        grammar.productions.add(Production(A165, mutableListOf(A3, U326)))
        grammar.productions.add(Production(A201, mutableListOf(A111, U)))
        grammar.productions.add(Production(A129, mutableListOf(A219, U)))
        grammar.productions.add(Production(A262, mutableListOf(Terminal('0'))))
        grammar.productions.add(Production(A263, mutableListOf(Terminal('1'))))
        grammar.productions.add(Production(A28, mutableListOf(Terminal('0'))))
        grammar.productions.add(Production(A29, mutableListOf(Terminal('1'))))
        grammar.productions.add(Production(A12, mutableListOf(Terminal('0'))))
        grammar.productions.add(Production(A8, mutableListOf(Terminal('1'))))
        grammar.productions.add(Production(A106, mutableListOf(Terminal('0'))))
        grammar.productions.add(Production(A94, mutableListOf(Terminal('1'))))
        grammar.productions.add(Production(A120, mutableListOf(Terminal('0'))))
        grammar.productions.add(Production(A116, mutableListOf(Terminal('1'))))
        grammar.productions.add(Production(A84, mutableListOf(Terminal('0'))))
        grammar.productions.add(Production(A80, mutableListOf(Terminal('1'))))
        grammar.productions.add(Production(A226, mutableListOf(Terminal('0'))))
        grammar.productions.add(Production(A227, mutableListOf(Terminal('1'))))
        grammar.productions.add(Production(A21, mutableListOf(Terminal('1'))))
        grammar.productions.add(Production(A3, mutableListOf(Terminal('0'))))
        grammar.productions.add(Production(A111, mutableListOf(Terminal('0'))))
        grammar.productions.add(Production(A219, mutableListOf(Terminal('1'))))
        grammar.productions.add(Production(S, mutableListOf()))
        grammar.productions.add(Production(A287, mutableListOf(A80, A129)))
        grammar.productions.add(Production(A287, mutableListOf(A84, A201)))
        grammar.productions.add(Production(A287, mutableListOf(Terminal('0'))))
        grammar.productions.add(Production(A279, mutableListOf(A80, A129)))
        grammar.productions.add(Production(A279, mutableListOf(A84, A201)))
        grammar.productions.add(Production(A279, mutableListOf(Terminal('0'))))
        grammar.productions.add(Production(A71, mutableListOf(A262, A165)))
        grammar.productions.add(Production(A71, mutableListOf(A263, A183)))
        grammar.productions.add(Production(A71, mutableListOf(Terminal('1'))))
        grammar.productions.add(Production(A63, mutableListOf(A262, A165)))
        grammar.productions.add(Production(A63, mutableListOf(A263, A183)))
        grammar.productions.add(Production(A63, mutableListOf(Terminal('1'))))
        grammar.productions.add(Production(S, mutableListOf(A94, A63)))
        grammar.productions.add(Production(S, mutableListOf(A106, A279)))
        grammar.productions.add(Production(S, mutableListOf(A94, A71)))
        grammar.productions.add(Production(S, mutableListOf(A106, A287)))
        grammar.productions.add(Production(U, mutableListOf(Terminal('0'))))
        grammar.productions.add(Production(U326, mutableListOf(Terminal('1'))))

        val expectedWithSAsNonterminal = setOf(
            "S, S0, S0",
            "S, S2, S2",
            "S, S3, S3",
            "S, S5, S5",
            "S, S6, S6",
            "S, S4, S4",
            "S, S1, S1",
            "S, S4, S6",
            "S, S2, S4",
            "S, S0, S2",
            "S, S1, S5",
            "S, S0, S6"
        )
        val actualWithSAsNonterminal = execute(automaton, grammar).filter {
            it.nonterminal == S
        }.map { it.toString() }.toSet()
        assertEquals(expectedWithSAsNonterminal, actualWithSAsNonterminal)
    }

    @Test
    fun `test for grammar of pda accepting by empty stack`() {
        val automaton = TestAutomatons.DFA_0110011

        val S = Nonterminal("S")
        val grammar = ContextFreeGrammar(S)
        val A2 = grammar.addNonterminal("A2")
        val A36 = grammar.addNonterminal("A36")
        val A38 = grammar.addNonterminal("A38")
        val A43 = grammar.addNonterminal("A43")
        val A53 = grammar.addNonterminal("A53")
        val A75 = grammar.addNonterminal("A75")
        val A85 = grammar.addNonterminal("A85")
        val A87 = grammar.addNonterminal("A87")
        val A104 = grammar.addNonterminal("A104")
        val A121 = grammar.addNonterminal("A121")
        val A123 = grammar.addNonterminal("A123")
        val A128 = grammar.addNonterminal("A128")
        val A138 = grammar.addNonterminal("A138")
        val A171 = grammar.addNonterminal("A171")
        val A176 = grammar.addNonterminal("A176")
        val A188 = grammar.addNonterminal("A188")
        val A189 = grammar.addNonterminal("A189")
        val A193 = grammar.addNonterminal("A193")
        val A239 = grammar.addNonterminal("A239")
        val A240 = grammar.addNonterminal("A240")
        val A244 = grammar.addNonterminal("A244")
        val A259 = grammar.addNonterminal("A259")
        val A264 = grammar.addNonterminal("A264")
        val A274 = grammar.addNonterminal("A274")
        val U = grammar.addNonterminal("U")
        val U291 = grammar.addNonterminal("U291")
        grammar.productions.add(Production(A36, mutableListOf(A38, A53)))
        grammar.productions.add(Production(A36, mutableListOf(A43, A138)))
        grammar.productions.add(Production(A121, mutableListOf(A123, A53)))
        grammar.productions.add(Production(A121, mutableListOf(A128, A138)))
        grammar.productions.add(Production(A189, mutableListOf(A188, A2)))
        grammar.productions.add(Production(A189, mutableListOf(A193, A87)))
        grammar.productions.add(Production(A240, mutableListOf(A239, A2)))
        grammar.productions.add(Production(A240, mutableListOf(A244, A87)))
        grammar.productions.add(Production(A87, mutableListOf(A36, U291)))
        grammar.productions.add(Production(A53, mutableListOf(A121, U)))
        grammar.productions.add(Production(A138, mutableListOf(A189, U)))
        grammar.productions.add(Production(A2, mutableListOf(A240, U291)))
        grammar.productions.add(Production(A75, mutableListOf(Terminal('1'))))
        grammar.productions.add(Production(A85, mutableListOf(Terminal('0'))))
        grammar.productions.add(Production(A176, mutableListOf(Terminal('1'))))
        grammar.productions.add(Production(A171, mutableListOf(Terminal('0'))))
        grammar.productions.add(Production(A123, mutableListOf(Terminal('1'))))
        grammar.productions.add(Production(A128, mutableListOf(Terminal('0'))))
        grammar.productions.add(Production(A121, mutableListOf(Terminal('1'))))
        grammar.productions.add(Production(A189, mutableListOf(Terminal('0'))))
        grammar.productions.add(Production(A193, mutableListOf(Terminal('1'))))
        grammar.productions.add(Production(A188, mutableListOf(Terminal('0'))))
        grammar.productions.add(Production(A259, mutableListOf(Terminal('1'))))
        grammar.productions.add(Production(A264, mutableListOf(Terminal('0'))))
        grammar.productions.add(Production(A38, mutableListOf(Terminal('1'))))
        grammar.productions.add(Production(A43, mutableListOf(Terminal('0'))))
        grammar.productions.add(Production(A36, mutableListOf(Terminal('1'))))
        grammar.productions.add(Production(A240, mutableListOf(Terminal('0'))))
        grammar.productions.add(Production(A244, mutableListOf(Terminal('1'))))
        grammar.productions.add(Production(A239, mutableListOf(Terminal('0'))))
        grammar.productions.add(Production(S, mutableListOf()))
        grammar.productions.add(Production(A274, mutableListOf(A171, A2)))
        grammar.productions.add(Production(A274, mutableListOf(A176, A87)))
        grammar.productions.add(Production(A274, mutableListOf(Terminal('0'))))
        grammar.productions.add(Production(A104, mutableListOf(A259, A53)))
        grammar.productions.add(Production(A104, mutableListOf(A264, A138)))
        grammar.productions.add(Production(A104, mutableListOf(Terminal('1'))))
        grammar.productions.add(Production(S, mutableListOf(A75, A104)))
        grammar.productions.add(Production(S, mutableListOf(A85, A274)))
        grammar.productions.add(Production(U, mutableListOf(Terminal('1'))))
        grammar.productions.add(Production(U291, mutableListOf(Terminal('0'))))

        val expectedWithSAsNonterminal = setOf(
            "S, S1, S1",
            "S, S2, S2",
            "S, S6, S6",
            "S, S3, S3",
            "S, S0, S0",
            "S, S7, S7",
            "S, S5, S5",
            "S, S4, S4",
            "S, S1, S3",
            "S, S3, S5",
            "S, S5, S7",
            "S, S0, S4",
            "S, S2, S6",
            "S, S1, S7"
        )
        val actualWithSAsNonterminal = execute(automaton, grammar).filter {
            it.nonterminal == S
        }.map { it.toString() }.toSet()
        assertEquals(expectedWithSAsNonterminal, actualWithSAsNonterminal)
    }
}
