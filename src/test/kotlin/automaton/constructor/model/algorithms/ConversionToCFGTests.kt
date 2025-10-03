package automaton.constructor.model.algorithms

import automaton.constructor.model.TestAutomatons
import automaton.constructor.model.grammar.ContextFreeGrammar
import automaton.constructor.model.grammar.Nonterminal
import automaton.constructor.model.grammar.SimpleProduction
import automaton.constructor.model.grammar.Terminal
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.Collections.swap

class ConversionToCFGTests {
    fun nextPermutation(nums: List<String>) {
        var i = nums.size - 2
        while (i >= 0 && nums[i] >= nums[i + 1]) {
            i--
        }
        if (i >= 0) {
            var j = nums.size - 1
            while (nums[j] <= nums[i]) {
                j--
            }
            swap(nums, i, j)
        }

        reverse(nums, i + 1)
    }

    fun reverse(nums: List<String>, start: Int) {
        var i = start
        var j = nums.size - 1
        while (i < j) {
            swap(nums, i, j)
            i++
            j--
        }
    }

    fun factorial(n: Int): Int {
        var result = 1
        for (i in 2..n) {
            result *= i
        }
        return result
    }

    fun areGrammarsEqual(expected: ContextFreeGrammar, actual: ContextFreeGrammar): Boolean {
        val expectedNonterminalsValues = expected.nonterminals.map { it.value }.sorted()
        val expectedProductions = expected.productions.map { it.toString() }.toSet()
        repeat(factorial(expectedNonterminalsValues.size)) {
            for (i in actual.nonterminals.indices) {
                actual.nonterminals[i].value = expectedNonterminalsValues[i]
            }
            val actualProductions = actual.productions.map { it.toString() }.toSet()
            if (actualProductions.equals(expectedProductions)) {
                return true
            }
            nextPermutation(expectedNonterminalsValues)
        }
        return false
    }

    @Test
    fun `correct bracket sequence recogniser test`() {
        val actual = TestAutomatons.CORRECT_BRACKET_SEQUENCE_RECOGNISER.convertToCFG()

        val S = Nonterminal("S")
        val expected = ContextFreeGrammar(S)
        val A = expected.addNonterminal("A")
        val U = expected.addNonterminal("U")
        val U1 = expected.addNonterminal("U1")
        val Y = expected.addNonterminal("Y")
        expected.productions.add(SimpleProduction(A, mutableListOf(A, A)))
        expected.productions.add(SimpleProduction(A, mutableListOf(Y, U1)))
        expected.productions.add(SimpleProduction(A, mutableListOf(U, U1)))
        expected.productions.add(SimpleProduction(S, mutableListOf()))
        expected.productions.add(SimpleProduction(S, mutableListOf(A, A)))
        expected.productions.add(SimpleProduction(S, mutableListOf(Y, U1)))
        expected.productions.add(SimpleProduction(S, mutableListOf(U, U1)))
        expected.productions.add(SimpleProduction(U, mutableListOf(Terminal('('))))
        expected.productions.add(SimpleProduction(U1, mutableListOf(Terminal(')'))))
        expected.productions.add(SimpleProduction(Y, mutableListOf(U, A)))

        assertTrue(areGrammarsEqual(expected, actual))
    }

    @Test
    fun `same number of zeros and ones test`() {
        val actual = TestAutomatons.SAME_NUMBER_OF_ZEROS_AND_ONES.convertToCFG()

        val S = Nonterminal("S")
        val expected = ContextFreeGrammar(S)
        val A = expected.addNonterminal("A")
        val U = expected.addNonterminal("U")
        val U1 = expected.addNonterminal("U1")
        val Y = expected.addNonterminal("Y")
        expected.productions.add(SimpleProduction(A, mutableListOf(Y, U1)))
        expected.productions.add(SimpleProduction(A, mutableListOf(U, U1)))
        expected.productions.add(SimpleProduction(S, mutableListOf()))
        expected.productions.add(SimpleProduction(S, mutableListOf(Y, U1)))
        expected.productions.add(SimpleProduction(S, mutableListOf(U, U1)))
        expected.productions.add(SimpleProduction(U, mutableListOf(Terminal('0'))))
        expected.productions.add(SimpleProduction(U1, mutableListOf(Terminal('1'))))
        expected.productions.add(SimpleProduction(Y, mutableListOf(U, A)))

        assertTrue(areGrammarsEqual(expected, actual))
    }

    @Test
    fun `same number of zeros and ones by empty stack test`() {
        val actual = TestAutomatons.SAME_NUMBER_OF_ZEROS_AND_ONES_BY_EMPTY_STACK.convertToCFG()

        val S = Nonterminal("S")
        val expected = ContextFreeGrammar(S)
        val A = expected.addNonterminal("A")
        val U = expected.addNonterminal("U")
        val U1 = expected.addNonterminal("U1")
        val Y = expected.addNonterminal("Y")
        expected.productions.add(SimpleProduction(A, mutableListOf(Y, U1)))
        expected.productions.add(SimpleProduction(A, mutableListOf(U, U1)))
        expected.productions.add(SimpleProduction(S, mutableListOf()))
        expected.productions.add(SimpleProduction(S, mutableListOf(Y, U1)))
        expected.productions.add(SimpleProduction(S, mutableListOf(U, U1)))
        expected.productions.add(SimpleProduction(U, mutableListOf(Terminal('0'))))
        expected.productions.add(SimpleProduction(U1, mutableListOf(Terminal('1'))))
        expected.productions.add(SimpleProduction(Y, mutableListOf(U, A)))

        assertTrue(areGrammarsEqual(expected, actual))
    }
}