package automaton.constructor.model.automaton

import automaton.constructor.model.element.AutomatonVertex
import automaton.constructor.model.element.RecursiveAutomatonBox
import automaton.constructor.model.grammar.EBNFGrammar
import automaton.constructor.model.property.EPSILON_VALUE
import automaton.constructor.model.property.FormalRegex

/**
 * Exports [RecursiveAutomaton] into an approximate [EBNFGrammar].
 */
object RecursiveAutomatonEBNFExporter {

    data class ExportResult(val grammar: EBNFGrammar, val warnings: List<String>)

    fun export(root: RecursiveAutomaton): ExportResult {
        val warnings = mutableListOf<String>()

        val closure = collectClosure(root)

        val nameMap = assignNames(root, closure)

        val grammar = EBNFGrammar()
        val nonterminals = closure.associateWith { grammar.findOrAddNonterminal(nameMap.getValue(it)) }

        val derivations = closure.associateWith { mutableSetOf<List<String>>() }
        closure.forEach { ra ->
            val localWarnings = mutableListOf<String>()
            collectDerivations(
                automaton = ra,
                exportedName = nameMap.getValue(ra),
                nameMap = nameMap,
                outSequences = derivations.getValue(ra),
                warnings = localWarnings
            )
            warnings += localWarnings
        }

        closure.sortedBy { nameMap[it] }.forEach { ra ->
            val nt = nonterminals.getValue(ra)
            val rhs = factorSequences(derivations.getValue(ra))
            grammar.addProduction(nt, rhs)
        }

        grammar.initialNonterminal = nonterminals.getValue(root)

        return ExportResult(grammar, warnings)
    }

    private const val EPSILON_TOKEN = "$"
    private const val MAX_DEPTH = 200

    private fun collectClosure(root: RecursiveAutomaton): Set<RecursiveAutomaton> {
        val seen = mutableSetOf<RecursiveAutomaton>()
        fun dfs(current: RecursiveAutomaton) {
            if (!seen.add(current)) return
            current.vertices.filterIsInstance<RecursiveAutomatonBox>().forEach { box ->
                (box.subAutomaton as? RecursiveAutomaton)?.let(::dfs)
            }
        }
        dfs(root)
        return seen
    }

    private fun assignNames(
        root: RecursiveAutomaton, closure: Set<RecursiveAutomaton>
    ): Map<RecursiveAutomaton, String> {
        val names = mutableMapOf<RecursiveAutomaton, String>()
        val rootName = sequenceOf(
            root.initialNonterminalNameSnapshot,
            root.grammar?.declaredInitialName,
            root.grammar?.initialNonterminal?.value,
            root.displayNameForMenu(),
            root.name
        ).firstOrNull { !it.isNullOrBlank() && !it.startsWith(root.untitledAdjective, ignoreCase = true) } ?: "S"

        names[root] = rootName

        var synthCounter = 0
        closure.filter { it !== root }.forEach { ra ->
            val raw = ra.name
            val candidate = if (raw.isBlank() || raw.startsWith(ra.untitledAdjective, true)) {
                ra.displayNameForMenu()
            } else raw
            val assigned = candidate.takeIf {
                !it.isNullOrBlank() && !it.startsWith(ra.untitledAdjective, true)
            } ?: "NT_${synthCounter++}"

            // Avoid collision with root
            names[ra] = if (assigned == rootName) "NT_${synthCounter++}" else assigned
        }
        return names
    }

    private data class Frame(
        val vertex: AutomatonVertex, val tokens: MutableList<String>, val depth: Int
    )

    /**
     * Collects token sequences from a single initial state to any final state.
     * Adds epsilon if no sequences found.
     */
    private fun collectDerivations(
        automaton: RecursiveAutomaton,
        exportedName: String,
        nameMap: Map<RecursiveAutomaton, String>,
        outSequences: MutableSet<List<String>>,
        warnings: MutableList<String>
    ) {
        val initials = automaton.states.filter { it.isInitial }
        val finals = automaton.states.filter { it.isFinal }.toSet()

        if (initials.size != 1 || finals.isEmpty()) {
            warnings += "Automaton $exportedName has unsupported initial/final configuration; using epsilon."
            outSequences.add(emptyList())
            return
        }

        val start = initials.single()
        var truncatedCount = 0
        var complexLabelCount = 0

        val stack = ArrayDeque<Frame>()
        stack += Frame(start, mutableListOf(), depth = 0)

        val seenEpsilonState = mutableSetOf<Pair<AutomatonVertex, Int>>()

        while (stack.isNotEmpty()) {
            val frame = stack.removeLast()

            if (frame.vertex in finals) {
                outSequences += frame.tokens.toList()
            }

            if (frame.depth >= MAX_DEPTH) {
                truncatedCount++
                continue
            }

            if (frame.tokens.isEmpty()) {
                val signature = frame.vertex to 0
                if (!seenEpsilonState.add(signature)) continue
            }

            automaton.getOutgoingTransitions(frame.vertex).forEach { tr ->
                val label = tr.getProperty(automaton.inputTape.expectedChar).value
                val nextTokens = frame.tokens.toMutableList()
                val target = tr.target

                if (target is RecursiveAutomatonBox && label == EPSILON_VALUE) {
                    val sub = target.subAutomaton as? RecursiveAutomaton
                    if (sub != null) {
                        nextTokens += nameMap.getValue(sub)
                    }
                    automaton.getOutgoingTransitions(target).forEach { out ->
                        stack += Frame(out.target, nextTokens.toMutableList(), frame.depth + 1)
                    }
                } else {
                    when (label) {
                        EPSILON_VALUE -> Unit
                        is FormalRegex.Singleton -> nextTokens += label.char.toString()
                        else -> complexLabelCount++
                    }
                    stack += Frame(target, nextTokens, frame.depth + 1)
                }
            }
        }

        if (outSequences.isEmpty()) {
            outSequences.add(emptyList())
        }
        if (truncatedCount > 0) {
            warnings += "Automaton $exportedName derivations truncated at depth $MAX_DEPTH ($truncatedCount paths cut)."
        }
        if (complexLabelCount > 0) {
            warnings += "Automaton $exportedName has $complexLabelCount complex transition label(s) ignored."
        }
    }

    private fun factorSequences(sequences: Set<List<String>>): String {
        if (sequences.isEmpty()) return EPSILON_TOKEN
        if (sequences.size == 1) {
            val seq = sequences.first()
            return if (seq.isEmpty()) EPSILON_TOKEN else seq.joinToString(" ")
        }

        val ordered = sequences.toList()
        val first = ordered.first()
        var prefixLen = 0
        while (true) {
            val token = first.getOrNull(prefixLen) ?: break
            if (ordered.all { it.getOrNull(prefixLen) == token }) {
                prefixLen++
            } else break
        }

        var suffixLen = 0
        while (true) {
            val idx = first.size - 1 - suffixLen
            if (idx < prefixLen) break
            val token = first.getOrNull(idx) ?: break
            if (ordered.all {
                    val j = it.size - 1 - suffixLen
                    j >= prefixLen && it.getOrNull(j) == token
                }) {
                suffixLen++
            } else break
        }

        val prefix = first.take(prefixLen)
        val suffix = if (suffixLen == 0) emptyList() else first.takeLast(suffixLen)

        val cores = ordered.map { it.subList(prefixLen, it.size - suffixLen) }.toSet()
        val coreStrings = cores.map { core ->
            when {
                core.isEmpty() -> EPSILON_TOKEN
                else -> core.joinToString(" ")
            }
        }.sorted()

        val multipleAlternatives = coreStrings.size > 1
        val hasContext = prefix.isNotEmpty() || suffix.isNotEmpty()
        val coreCombined = if (coreStrings.size == 1) coreStrings.first() else coreStrings.joinToString(" | ")
        val wrap = multipleAlternatives && hasContext

        return buildString {
            if (prefix.isNotEmpty()) {
                append(prefix.joinToString(" "))
                append(' ')
            }
            if (wrap) append('(')
            append(coreCombined)
            if (wrap) append(')')
            if (suffix.isNotEmpty()) {
                append(' ')
                append(suffix.joinToString(" "))
            }
        }.trim().ifBlank { EPSILON_TOKEN }
    }
}