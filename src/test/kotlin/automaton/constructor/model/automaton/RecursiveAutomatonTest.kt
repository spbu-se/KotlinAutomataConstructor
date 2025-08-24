package automaton.constructor

import automaton.constructor.model.automaton.AbstractAutomatonTest
import automaton.constructor.model.automaton.RecursiveAutomaton
import automaton.constructor.model.automaton.RecursiveAutomatonBuilder
import automaton.constructor.model.factory.RecursiveAutomatonFactory
import automaton.constructor.model.grammar.EBNFGrammar
import automaton.constructor.model.grammar.Nonterminal
import automaton.constructor.model.memory.tape.InputTapeDescriptor
import automaton.constructor.model.module.executor.StepByClosureStrategy
import automaton.constructor.model.module.executor.executor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RecursiveAutomatonTest : AbstractAutomatonTest() {
    // Build an automaton from compact spec, e.g. "S; S -> 'a' S | 'b'"
    private fun buildAutomaton(spec: String): RecursiveAutomaton {
        val parts = spec.split(';')
        val init = parts[0].trim()
        val rulesPart = parts[1]
        val grammar = EBNFGrammar()
        val ntMap = mutableMapOf<String, Nonterminal>()
        fun nt(name: String) = ntMap.getOrPut(name) { Nonterminal(name) }
        grammar.initialNonterminal = nt(init)
        rulesPart.split(',').forEach { raw ->
            val rule = raw.trim()
            if (rule.isEmpty()) return@forEach
            val (l, r) = rule.split("->").map { it.trim() }
            val leftNt = nt(l)
            r.split('|').forEach { alt -> grammar.addProduction(leftNt, alt.trim()) }
        }
        val factory = RecursiveAutomatonFactory().apply { this.grammar = grammar }
        val ra = factory.createAutomaton()

        return ra
    }

    private fun accepts(ra: RecursiveAutomaton, input: String, maxMillis: Long = 2000): Boolean {
        ra.inputTape.value = input
        val exec = ra.executor
        exec.start()
        exec.runFor(maxMillis = maxMillis, strategy = StepByClosureStrategy)
        return exec.status.name == "ACCEPTED"
    }

    private fun buildGrammarForRegistryTest(): EBNFGrammar {
        val g = EBNFGrammar()
        val s = Nonterminal("S")
        val n = Nonterminal("N")
        g.initialNonterminal = s
        g.addProduction(s, "N S")
        g.addProduction(s, "'b'")
        g.addProduction(n, "'a'")
        return g
    }

    @Test
    fun testRightRecursion() {
        val ra = buildAutomaton("S; S -> 'a' S | 'b'")
        listOf("b", "ab", "aaab", "aaaab").forEach { w ->
            val res = accepts(ra, w)
            assertTrue(res, "Should accept $w for grammar S -> 'a'S | 'b'")
        }
    }

    @Test
    fun testLeftRecursion() {
        val ra = buildAutomaton("S; S -> S 'a' | 'b'")
        listOf("b", "ba", "baa", "baaa").forEach { w ->
            val res = accepts(ra, w)
            assertTrue(res, "Should accept $w for grammar S -> S'a' | 'b'")
        }
    }

    @Test
    fun testStringLiterals() {
        val ra = buildAutomaton("S; S -> \"ab\" S | \"cd\"")
        listOf("cd", "abcd", "ababcd").forEach { w ->
            val res = accepts(ra, w)

            assertTrue(res, "Should accept $w for grammar S -> \"ab\"S | \"cd\"")
        }
    }

    @Test
    fun debugRightRecursion() {
        val ra = buildAutomaton("Start; Start -> N Start | 'b', N -> 'a'")
        assertTrue(ra.transitions.isNotEmpty(), "Root automaton should have transitions")
        listOf("b", "ab", "aab").forEach { w ->
            val res = accepts(ra, w)
            assertTrue(res, "Should accept $w")
        }
    }

    @Test
    fun debugConsecutiveNonterminals() {
        // Grammar: Start -> A B | 'x'; A -> 'a'; B -> 'b'
        val ra = buildAutomaton("Start; Start -> A B | 'x', A -> 'a', B -> 'b'")
        assertTrue(ra.transitions.isNotEmpty(), "Root automaton should have transitions for consecutive nonterminals")
        listOf("ab", "x").forEach { w ->
            assertTrue(accepts(ra, w), "Should accept $w")
        }
        assertTrue(!accepts(ra, "a"), "Should not accept lone 'a'")
    }

    @Test
    fun debugTerminalStringConcat() {
        // Grammar: Start -> 'a' Start "bcd" | 'd'  (language: a^n d (bcd)^n )
        val ra = buildAutomaton("Start; Start -> 'a' Start \"bcd\" | 'd'")
        listOf("d", "adbcd", "aadbcdbcd").forEach { w ->
            assertTrue(accepts(ra, w), "Should accept $w")
        }
        assertTrue(!accepts(ra, "ad"), "Should not accept ad (missing trailing bcd)")
    }

    @Test
    fun `empty sub-automaton is removed from registry after box deletion`() {
        val root = RecursiveAutomaton(InputTapeDescriptor())
        val sub = root.createEmptyAutomatonOfSameType()
        assertFalse(
            sub in RecursiveAutomaton.allInstances(),
            "Sub-automaton should not be registered before being referenced by a box"
        )
        val box = root.addRecursiveAutomatonBox(subAutomaton = sub, bindName = true)
        assertTrue(sub in RecursiveAutomaton.allInstances(), "Sub-automaton should be registered after box added")
        root.removeVertex(box)
        assertFalse(
            sub in RecursiveAutomaton.allInstances(),
            "Sub-automaton should be unregistered after its only referencing box is removed and it is empty"
        )
        assertTrue(root in RecursiveAutomaton.allInstances(), "Root automaton should remain registered")
        val finalSet = RecursiveAutomaton.allInstances()
        assertTrue(root in finalSet && sub !in finalSet, "Registry must contain root but not sub after cleanup")
    }

    @Test
    fun `registry size does not grow after repeated rebuilds`() {
        val before = RecursiveAutomaton.allInstances().size
        val grammar = buildGrammarForRegistryTest()
        val factory = RecursiveAutomatonFactory().apply { this.grammar = grammar }
        val root = factory.createAutomaton()
        val afterFirst = RecursiveAutomaton.allInstances().size
        val delta = afterFirst - before
        assertTrue(delta >= 2, "Expected at least root and one sub-automaton added to registry; delta=$delta")
        repeat(5) { idx ->
            root.grammar = grammar
            RecursiveAutomatonBuilder.buildFromGrammar(root, grammar)
            val current = RecursiveAutomaton.allInstances().size
            assertEquals(
                afterFirst,
                current,
                "Registry size changed after rebuild #$idx (delta=${current - afterFirst})"
            )
        }
    }

    override fun createAutomaton() = RecursiveAutomaton(InputTapeDescriptor())

}
