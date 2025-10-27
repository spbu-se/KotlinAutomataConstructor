package automaton.constructor.model.automaton

import automaton.constructor.model.automaton.recursive.RecursiveAutomaton
import automaton.constructor.model.automaton.recursive.RecursiveAutomatonBuilder
import automaton.constructor.model.factory.RecursiveAutomatonFactory
import automaton.constructor.model.grammar.EBNFGrammar
import automaton.constructor.model.grammar.Nonterminal
import automaton.constructor.model.grammar.RARegex
import automaton.constructor.model.memory.tape.InputTapeDescriptor
import automaton.constructor.model.module.executor.StepByClosureStrategy
import automaton.constructor.model.module.executor.executor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for [automaton.constructor.model.automaton.recursive.RecursiveAutomaton].
 */
class RecursiveAutomatonTest : AbstractAutomatonTest() {

    private fun RecursiveAutomaton.accepts(
        input: String, maxMillis: Long = DEFAULT_EXECUTION_TIMEOUT_MS
    ): Boolean {
        inputTape.value = input
        val exec = executor
        exec.start()
        exec.runFor(maxMillis = maxMillis, strategy = StepByClosureStrategy)
        return exec.status.name == "ACCEPTED"
    }

    private fun assertAcceptsAll(
        automaton: RecursiveAutomaton, words: Iterable<String>, grammarInfo: String
    ) {
        words.forEach { w ->
            assertTrue(
                automaton.accepts(w), "Expected automaton to accept '$w' for grammar: $grammarInfo"
            )
        }
    }

    private fun assertRejectsAll(
        automaton: RecursiveAutomaton, words: Iterable<String>, grammarInfo: String
    ) {
        words.forEach { w ->
            assertFalse(
                automaton.accepts(w), "Expected automaton to reject '$w' for grammar: $grammarInfo"
            )
        }
    }

    private fun buildAutomaton(spec: String): RecursiveAutomaton {
        require(';' in spec) {
            "Spec must contain ';' separating initial nonterminal and rules. Got: $spec"
        }
        val (initialRaw, rulesRaw) = spec.split(';', limit = 2)
        val initialName = initialRaw.trim().ifEmpty {
            error("Initial nonterminal name is empty in spec: $spec")
        }
        val grammar = EBNFGrammar()
        val nonterminals = mutableMapOf<String, Nonterminal>()
        fun nt(name: String) = nonterminals.getOrPut(name.trim()) { Nonterminal(name.trim()) }

        grammar.initialNonterminal = nt(initialName)

        rulesRaw.split(',').map { it.trim() }.filter { it.isNotEmpty() }.forEach { rule ->
                val arrowIndex = rule.indexOf("->")
                require(arrowIndex >= 0) { "Rule must contain '->': $rule (spec: $spec)" }
                val leftName = rule.take(arrowIndex).trim()
                val rightPart = rule.substring(arrowIndex + 2).trim()
                require(leftName.isNotEmpty()) { "Left side nonterminal is empty in rule: $rule" }
                require(rightPart.isNotEmpty()) { "Right side productions are empty in rule: $rule" }
                val leftNt = nt(leftName)
                rightPart.split('|').map { it.trim() }.filter { it.isNotEmpty() }.forEach { alt ->
                        val regex =
                            RARegex.parse(alt) ?: error("Parsed null RARegex for alternative '$alt' in rule '$rule'")
                        grammar.addProduction(leftNt, regex)
                    }
            }

        return RecursiveAutomatonFactory().apply { this.grammar = grammar }.createAutomaton()
    }

    private fun buildRegistryTestGrammar(): EBNFGrammar = EBNFGrammar().apply {
        val s = Nonterminal("S")
        val n = Nonterminal("N")
        initialNonterminal = s
        addProduction(s, RARegex.parse("N S")!!)
        addProduction(s, RARegex.parse("'b'")!!)   // or RARegex.parse("b") if parser supports raw
        addProduction(n, RARegex.parse("'a'")!!)
    }

    @Test
    fun `accepts right-recursive grammar a^n b`() {
        val spec = "S; S -> 'a' S | 'b'"
        val ra = buildAutomaton(spec)
        assertAcceptsAll(ra, listOf("b", "ab", "aaab", "aaaab"), spec)
    }

    @Test
    fun `accepts left-recursive grammar b a^n`() {
        val spec = "S; S -> S 'a' | 'b'"
        val ra = buildAutomaton(spec)
        assertAcceptsAll(ra, listOf("b", "ba", "baa", "baaa"), spec)
    }

    @Test
    fun `accepts grammar with string literals and concatenation`() {
        val spec = "S; S -> \"ab\" S | \"cd\""
        val ra = buildAutomaton(spec)
        assertAcceptsAll(ra, listOf("cd", "abcd", "ababcd"), spec)
    }

    @Test
    fun `accepts grammar with intermediate nonterminal expansion`() {
        val spec = "Start; Start -> N Start | 'b', N -> 'a'"
        val ra = buildAutomaton(spec)
        assertTrue(ra.transitions.isNotEmpty(), "Root automaton should have transitions")
        assertAcceptsAll(ra, listOf("b", "ab", "aab"), spec)
    }

    @Test
    fun `handles consecutive nonterminals A B`() {
        val spec = "Start; Start -> A B | 'x', A -> 'a', B -> 'b'"
        val ra = buildAutomaton(spec)
        assertTrue(
            ra.transitions.isNotEmpty(), "Root automaton should have transitions for consecutive nonterminals"
        )
        assertAcceptsAll(ra, listOf("ab", "x"), spec)
        assertRejectsAll(ra, listOf("a"), spec)
    }

    @Test
    fun `handles nested terminal concatenation pattern a^n d (bcd)^n`() {
        val spec = "Start; Start -> 'a' Start \"bcd\" | 'd'"
        val ra = buildAutomaton(spec)
        assertAcceptsAll(ra, listOf("d", "adbcd", "aadbcdbcd"), spec)
        assertRejectsAll(ra, listOf("ad"), spec) // Missing trailing "bcd"
    }

    @Test
    fun `removes empty sub-automaton from registry after box deletion`() {
        val root = RecursiveAutomaton(InputTapeDescriptor())
        val sub = root.createEmptyAutomatonOfSameType()

        assertFalse(
            sub in RecursiveAutomaton.allInstances(),
            "Sub-automaton should not be registered before being referenced by a box"
        )

        val box = root.addRecursiveAutomatonBox(subAutomaton = sub, bindName = true)
        assertTrue(
            sub in RecursiveAutomaton.allInstances(), "Sub-automaton should be registered after the box is added"
        )

        root.removeVertex(box)

        assertFalse(
            sub in RecursiveAutomaton.allInstances(),
            "Sub-automaton should be unregistered after its only referencing box is removed and it remains empty"
        )

        assertTrue(
            root in RecursiveAutomaton.allInstances(), "Root automaton must remain registered"
        )

        val finalSet = RecursiveAutomaton.allInstances()
        assertTrue(
            root in finalSet && sub !in finalSet, "Registry must contain root but not sub after cleanup"
        )
    }

    @Test
    fun `keeps registry size stable after repeated rebuilds`() {
        val before = RecursiveAutomaton.allInstances().size
        val grammar = buildRegistryTestGrammar()
        val factory = RecursiveAutomatonFactory().apply { this.grammar = grammar }
        val root = factory.createAutomaton()

        val afterFirst = RecursiveAutomaton.allInstances().size
        val delta = afterFirst - before
        assertTrue(
            delta >= 2, "Expected at least root and one sub-automaton added to registry; delta=$delta"
        )

        repeat(REBUILD_REPEAT_COUNT) { idx ->
            root.grammar = grammar
            RecursiveAutomatonBuilder.buildFromGrammar(root, grammar)
            val current = RecursiveAutomaton.allInstances().size
            assertEquals(
                afterFirst, current, "Registry size changed after rebuild #$idx (delta=${current - afterFirst})"
            )
        }
    }

    override fun createAutomaton(): RecursiveAutomaton = RecursiveAutomaton(InputTapeDescriptor())

    companion object {
        private const val DEFAULT_EXECUTION_TIMEOUT_MS = 2_000L
        private const val REBUILD_REPEAT_COUNT = 5
    }
}