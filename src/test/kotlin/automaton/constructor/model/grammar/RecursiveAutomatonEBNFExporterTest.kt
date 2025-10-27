package automaton.constructor.model.grammar

import automaton.constructor.model.automaton.recursive.RecursiveAutomaton
import automaton.constructor.model.automaton.recursive.RecursiveAutomatonEBNFExporter
import automaton.constructor.model.data.createAutomaton
import automaton.constructor.model.data.getData
import automaton.constructor.model.factory.RecursiveAutomatonFactory
import automaton.constructor.model.serializers.JsonAutomatonSerializer
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RecursiveAutomatonEBNFExporterTest {

    private fun baseGrammar(): EBNFGrammar = EBNFGrammar().apply {
        val s = Nonterminal("S")
        val n = Nonterminal("N")
        initialNonterminal = s
        addProduction(s, RARegex.parse("N S")!!)
        addProduction(s, RARegex.parse("'b'")!!)  // or just b
        addProduction(n, RARegex.parse("'a'")!!)
    }

    private fun factoringGrammar(): EBNFGrammar = EBNFGrammar().apply {
        val A = Nonterminal("A")
        val B = Nonterminal("B")
        initialNonterminal = A
        addProduction(A, RARegex.parse("'a' A 'b'")!!)
        addProduction(A, RARegex.parse("'a' B 'b'")!!)
        addProduction(B, RARegex.parse("'c'")!!)
    }

    private fun createAutomatonFromGrammar(grammar: EBNFGrammar): RecursiveAutomaton =
        RecursiveAutomatonFactory().apply { this.grammar = grammar }.createAutomaton()

    private fun export(ra: RecursiveAutomaton) = RecursiveAutomatonEBNFExporter.export(ra)

    private fun EBNFGrammar.rightSidesOf(nonterminal: String): List<RARegex> =
        productions.filter { it.leftSide.value == nonterminal }.map { it.rightSide }

    private fun Iterable<RARegex>.rendered(): List<String> = map { it.render() }

    @Test
    fun `exports simple grammar with expected productions`() {
        val grammar = baseGrammar()
        val ra = createAutomatonFromGrammar(grammar)
        val (exported, warnings) = export(ra)

        assertEquals("S", exported.initialNonterminal.value)

        val sRhs = exported.rightSidesOf("S").rendered()
        val joined = sRhs.joinToString(" | ")
        assertTrue(joined.contains("N S"), "Missing recursive alternative for S: $joined")
        assertTrue(joined.contains("b") || joined.contains("'b'"), "Missing terminal alternative 'b' for S: $joined")

        val nRhs = exported.rightSidesOf("N").rendered()
        assertTrue(nRhs.any { it == "a" || it == "'a'" }, "Expected N -> 'a', got $nRhs")

        assertTrue(warnings.size <= 2, "Unexpected number of warnings: $warnings")
    }

    @Test
    fun `exports factored or original alternatives for common prefix`() {
        val g = factoringGrammar()
        val ra = createAutomatonFromGrammar(g)
        val (exported, _) = export(ra)

        val aRhs = exported.rightSidesOf("A").rendered()
        val combined = aRhs.joinToString(" | ")

        // Accept either factored or original
        val factoredPattern1 = "a (A | B) b"
        val factoredPattern2 = "a (B | A) b"
        val hasFactored = combined.contains(factoredPattern1) || combined.contains(factoredPattern2)

        val hasOriginals =
            (combined.contains("a A b") && combined.contains("a B b")) || (combined.contains("'a' A 'b'") && combined.contains(
                "'a' B 'b'"
            ))

        assertTrue(
            hasFactored || hasOriginals, "Expected either factored form or originals; got: $combined"
        )
    }

    @Test
    fun `preserves initial symbol after serialization round trip`() {
        val g = baseGrammar()
        val ra = createAutomatonFromGrammar(g)

        val originalData = ra.getData()
        val targetFile = File("build/tmp/ra_roundtrip.atmtn").apply { parentFile.mkdirs() }
        JsonAutomatonSerializer.serialize(targetFile, originalData)

        val loadedData = JsonAutomatonSerializer.deserialize(targetFile)
        val loaded = loadedData.createAutomaton() as RecursiveAutomaton
        if (loaded.grammar == null) loaded.grammar = g

        val (exported, _) = export(loaded)
        assertEquals("S", exported.initialNonterminal.value)
    }
}