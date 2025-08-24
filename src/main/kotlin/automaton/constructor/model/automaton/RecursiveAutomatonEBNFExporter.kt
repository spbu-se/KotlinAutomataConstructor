package automaton.constructor.model.automaton

import automaton.constructor.model.element.EBNFGrammar
import automaton.constructor.model.element.Nonterminal
import automaton.constructor.model.element.RecursiveAutomatonBox
import automaton.constructor.model.property.EPSILON_VALUE
import automaton.constructor.model.property.FormalRegex

/**
 *  Exporter from a [RecursiveAutomaton] hierarchy back to an [EBNFGrammar].
 */
object RecursiveAutomatonEBNFExporter {
    data class ExportResult(val grammar: EBNFGrammar, val warnings: List<String>)

    private fun factor(seqs: Set<List<String>>): String {
        if (seqs.isEmpty()) return "$"
        if (seqs.size == 1) return seqs.first().joinToString(" ") { it }
        var prefixLen = 0
        while (true) {
            val firstTok = seqs.first().getOrNull(prefixLen) ?: break
            if (seqs.all { it.getOrNull(prefixLen) == firstTok }) prefixLen++ else break
        }
        var suffixLen = 0
        while (true) {
            val firstSeq = seqs.first()
            val idx = firstSeq.size - 1 - suffixLen
            if (idx < prefixLen) break
            val tok = firstSeq.getOrNull(idx) ?: break
            if (seqs.all {
                    val id2 = it.size - 1 - suffixLen; id2 >= prefixLen && it.getOrNull(id2) == tok
                }) suffixLen++ else break
        }
        val prefix = seqs.first().take(prefixLen)
        val suffix = if (suffixLen == 0) emptyList() else seqs.first().takeLast(suffixLen)
        val cores = seqs.map { it.subList(prefixLen, it.size - suffixLen) }.toSet()
        val coreStrings = cores.map { if (it.isEmpty()) "$" else it.joinToString(" ") }.sorted()
        val coreCombined = if (coreStrings.size == 1) coreStrings.first() else coreStrings.joinToString(" | ")
        val needsParens = coreStrings.size > 1 && (prefix.isNotEmpty() || suffix.isNotEmpty())
        return buildString {
            if (prefix.isNotEmpty()) append(prefix.joinToString(" ")).append(' ')
            if (needsParens) append('(')
            append(coreCombined)
            if (needsParens) append(')')
            if (suffix.isNotEmpty()) {
                append(' ')
                append(suffix.joinToString(" "))
            }
        }.trim().ifBlank { "$" }
    }

    fun export(root: RecursiveAutomaton): ExportResult {
        val warnings = mutableListOf<String>()
        val grammar = EBNFGrammar()
        // discover closure of reachable recursive automatons
        val closure = mutableSetOf<RecursiveAutomaton>()
        fun dfsA(ra: RecursiveAutomaton) {
            if (!closure.add(ra)) return
            ra.vertices.filterIsInstance<RecursiveAutomatonBox>().forEach { box ->
                (box.subAutomaton as? RecursiveAutomaton)?.let { dfsA(it) }
            }
        }
        dfsA(root)

        val nameMap = mutableMapOf<RecursiveAutomaton, String>()
        val rootInitialName = sequenceOf(
            root.initialNonterminalNameSnapshot, // snapshot first (original grammar symbol)
            root.grammar?.declaredInitialName,
            root.grammar?.initialNonterminal?.value, // current grammar value (may have drifted)
            root.displayNameForMenu(),
            root.name
        ).filterNot { it.isNullOrBlank() || it.startsWith(root.untitledAdjective, true) }.firstOrNull()
            ?: "S" // set S as default
        nameMap[root] = rootInitialName

        var syntheticIdx = 0
        closure.filter { it !== root }.forEach { ra ->
            val raw = ra.name
            val candidate =
                if (raw.isBlank() || raw.startsWith(ra.untitledAdjective, true)) ra.displayNameForMenu() else raw
            val cleaned = candidate.takeIf { it.isNotBlank() && !it.startsWith(ra.untitledAdjective, true) }
                ?: "NT_${syntheticIdx++}"
            nameMap[ra] = if (cleaned == rootInitialName && ra !== root) "NT_${syntheticIdx++}" else cleaned
        }
        val ntMap = mutableMapOf<RecursiveAutomaton, Nonterminal>()
        closure.forEach { ra ->
            val name = nameMap.getValue(ra)
            ntMap[ra] = grammar.findOrAddNonterminal(name)
        }
        grammar.initialNonterminal = ntMap.getValue(root)

        val seqsPerAutomaton = mutableMapOf<RecursiveAutomaton, MutableSet<List<String>>>().apply {
            closure.forEach { put(it, mutableSetOf()) }
        }
        closure.forEach { ra ->
            val startStates = ra.states.filter { it.isInitial }
            val finalStates = ra.states.filter { it.isFinal }
            if (startStates.size != 1 || finalStates.isEmpty()) {
                warnings += "Automaton ${nameMap.getValue(ra)} has unsupported initial/final configuration; skipping"
                return@forEach
            }
            val start = startStates.single()
            val finals = finalStates.toSet()

            data class Frame(
                val vertex: automaton.constructor.model.element.AutomatonVertex,
                val tokens: MutableList<String>,
                val depth: Int
            )

            val stack = ArrayDeque<Frame>()
            stack.add(Frame(start, mutableListOf(), 0))
            var pathCutoffs = 0
            val maxDepth = 200
            while (stack.isNotEmpty()) {
                val f = stack.removeLast()
                if (f.vertex in finals) seqsPerAutomaton.getValue(ra).add(f.tokens.toList())
                if (f.depth > maxDepth) {
                    pathCutoffs++; continue
                }
                ra.getOutgoingTransitions(f.vertex).forEach { tr ->
                    val label = tr.getProperty(ra.inputTape.expectedChar).value
                    val nextTokens = f.tokens.toMutableList()
                    val tgt = tr.target
                    if (tgt is RecursiveAutomatonBox && label == EPSILON_VALUE) {
                        val subRa = tgt.subAutomaton as? RecursiveAutomaton
                        if (subRa != null) nextTokens.add(nameMap.getValue(subRa))
                        ra.getOutgoingTransitions(tgt).forEach { out ->
                            stack.add(Frame(out.target, nextTokens.toMutableList(), f.depth + 1))
                        }
                    } else {
                        when (label) {
                            EPSILON_VALUE -> {}
                            is FormalRegex.Singleton -> nextTokens.add(label.char.toString())
                            else -> warnings += "Complex regex flattened in ${nameMap.getValue(ra)}"
                        }
                        stack.add(Frame(tgt, nextTokens, f.depth + 1))
                    }
                }
            }
            if (pathCutoffs > 0) warnings += "Truncated long derivations for ${nameMap.getValue(ra)}: $pathCutoffs cut off"
            if (seqsPerAutomaton.getValue(ra).isEmpty()) seqsPerAutomaton.getValue(ra).add(emptyList())
        }

        seqsPerAutomaton.forEach { (ra, seqs) ->
            val left = ntMap.getValue(ra)
            val factored = factor(seqs)
            grammar.addProduction(left, factored)
        }
        return ExportResult(grammar, warnings)
    }
}
