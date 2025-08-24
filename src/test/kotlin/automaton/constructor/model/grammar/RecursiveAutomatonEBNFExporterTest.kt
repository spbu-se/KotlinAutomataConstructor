package automaton.constructor.model.grammar

import automaton.constructor.model.automaton.RecursiveAutomaton
import automaton.constructor.model.automaton.RecursiveAutomatonEBNFExporter
import automaton.constructor.model.data.createAutomaton
import automaton.constructor.model.data.getData
import automaton.constructor.model.factory.RecursiveAutomatonFactory
import automaton.constructor.model.serializers.JsonAutomatonSerializer
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RecursiveAutomatonEBNFExporterTest {
    private fun buildGrammar(): EBNFGrammar {
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
    fun exportSimpleGrammar() {
        val grammar = buildGrammar()
        val factory = RecursiveAutomatonFactory().apply { this.grammar = grammar }
        val ra = factory.createAutomaton()
        val (exported, warnings) = RecursiveAutomatonEBNFExporter.export(ra)
        assertEquals("S", exported.initialNonterminal.value)
        val sProductions = exported.productions.filter { it.leftSide.value == "S" }.map { it.rightSide }
        val alts = sProductions.flatMap { it.split('|') }.map { it.trim() }
        assertTrue(alts.any { it.replace(" ", "") == "NS" }, "Expected alternative NS in S productions: $sProductions")
        assertTrue(alts.any { it.replace(" ", "") == "b" }, "Expected alternative b in S productions: $sProductions")
        val nProductions = exported.productions.filter { it.leftSide.value == "N" }.map { it.rightSide }
        assertTrue(nProductions.any { it.trim() == "a" }, "Expected production N -> a, got $nProductions")
        assertTrue(warnings.size <= 2, "Unexpectedly many warnings: $warnings")
    }

    @Test
    fun exportFactoringGrammar() {
        val g = EBNFGrammar()
        val A = Nonterminal("A")
        val B = Nonterminal("B")
        g.initialNonterminal = A
        g.addProduction(A, "'a' A 'b'")
        g.addProduction(A, "'a' B 'b'")
        g.addProduction(B, "'c'")
        val factory = RecursiveAutomatonFactory().apply { this.grammar = g }
        val ra = factory.createAutomaton()
        val (exported, _) = RecursiveAutomatonEBNFExporter.export(ra)
        val aProduction = exported.productions.first { it.leftSide.value == "A" }.rightSide
        val normalized = aProduction.replace(Regex("\\s+"), " ").trim()
        val variants = setOf("a (A | B) b", "a (B | A) b")
        assertTrue(normalized in variants, "Expected factored production 'a (A | B) b' got '$aProduction'")
    }

    @Test
    fun exportAfterSaveLoadKeepsInitialSymbol() {
        val g = buildGrammar()
        val factory = RecursiveAutomatonFactory().apply { this.grammar = g }
        val ra = factory.createAutomaton()
        val data = ra.getData()
        val f = File("build/tmp/ra_roundtrip.atmtn").apply { parentFile.mkdirs() }
        JsonAutomatonSerializer.serialize(f, data)
        val loadedData = JsonAutomatonSerializer.deserialize(f)
        val loaded = loadedData.createAutomaton() as RecursiveAutomaton
        if (loaded.grammar == null) loaded.grammar = g
        val (exp, _) = RecursiveAutomatonEBNFExporter.export(loaded)
        assertEquals("S", exp.initialNonterminal.value, "Initial symbol after round-trip must remain S")
    }
}