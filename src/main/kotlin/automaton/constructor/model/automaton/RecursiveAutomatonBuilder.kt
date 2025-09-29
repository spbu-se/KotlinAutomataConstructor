package automaton.constructor.model.automaton

import automaton.constructor.model.element.RecursiveAutomatonBox
import automaton.constructor.model.element.State
import automaton.constructor.model.grammar.EBNFGrammar
import automaton.constructor.model.grammar.Nonterminal
import automaton.constructor.model.grammar.RARegex
import automaton.constructor.model.module.layout.static.ELKLayeredLayout
import automaton.constructor.model.module.layout.static.applyLayout
import automaton.constructor.model.module.layout.static.toElkGraphMapping
import automaton.constructor.model.property.EPSILON_VALUE
import automaton.constructor.model.property.FormalRegex
import javafx.geometry.BoundingBox
import javafx.geometry.Point2D
import org.eclipse.elk.core.RecursiveGraphLayoutEngine
import org.eclipse.elk.core.util.BasicProgressMonitor

/**
 * Builds a [RecursiveAutomaton] from an [EBNFGrammar].
 */
object RecursiveAutomatonBuilder {

    fun buildFromGrammar(root: RecursiveAutomaton, grammar: EBNFGrammar) {
        val previouslyReachable = collectReachableAutomata(root).apply { remove(root) }

        root.vertices.toList().forEach { root.removeVertex(it) }
        root.nameProperty.unbind()
        if (grammar.productions.isEmpty()) return

        val transformed = eliminateImmediateLeftRecursion(grammar)
        val initial = transformed.initialNonterminal

        val grouped = groupProductions(transformed)
        if (initial !in grouped.keys) return
        val parsed = parseAndNormalize(grouped)

        val infos = createAutomataForNonterminals(root, grouped.keys, initial)

        addReferenceBoxes(infos, grouped.keys, parsed)

        val (startStates, endStates) = createBoundaryStates(infos.values.map { it.sub })

        infos.values.forEach { (nt, sub) ->
            buildRegexIntoAutomaton(sub, parsed[nt], startStates.getValue(sub), endStates.getValue(sub))
        }

        infos.values.map { it.sub }.distinct().forEach { applyElkLayout(it) }

        val nowReachable = collectReachableAutomata(root).apply { remove(root) }
        val toRemove = previouslyReachable - nowReachable
        toRemove.forEach { old -> if (old.referenceCount == 0) RecursiveAutomaton.registry.remove(old) }
    }


    private data class Info(val nt: Nonterminal, val sub: RecursiveAutomaton)

    private fun groupProductions(g: EBNFGrammar): Map<Nonterminal, String> =
        g.productions.groupBy { it.leftSide }.mapValues { (_, ps) -> ps.joinToString("|") { it.rightSide.trim() } }

    private fun parseAndNormalize(grouped: Map<Nonterminal, String>): Map<Nonterminal, RARegex?> =
        grouped.mapValues { (_, rhs) -> normalizeRegex(parseRegex(rhs)) }

    private fun createAutomataForNonterminals(
        root: RecursiveAutomaton, nonterminals: Set<Nonterminal>, initial: Nonterminal
    ): Map<Nonterminal, Info> {
        if (root.name.isBlank() || root.name == root.untitledAdjective) root.name = initial.value
        val map = mutableMapOf<Nonterminal, Info>()
        map[initial] = Info(initial, root).also { root.name = initial.value }
        nonterminals.filter { it != initial }.forEach { nt ->
            val sub = root.createEmptyAutomatonOfSameType()
            sub.name = nt.value
            map[nt] = Info(nt, sub)
        }
        return map
    }

    private fun addReferenceBoxes(
        infos: Map<Nonterminal, Info>, allNts: Set<Nonterminal>, parsed: Map<Nonterminal, RARegex?>
    ) {
        infos.values.forEach { info ->
            val refs = collectReferencedNonterminals(parsed[info.nt])
            // Self‑reference always needed for recursion path correctness
            if (info.nt.value in refs) refs.add(info.nt.value)
            refs.forEach { refName ->
                allNts.firstOrNull { it.value == refName }?.let { refNt ->
                    val targetInfo = infos.getValue(refNt)
                    val host = info.sub
                    val already = host.vertices.filterIsInstance<RecursiveAutomatonBox>().any { it.name == refNt.value }
                    if (!already) host.addRecursiveAutomatonBox(
                        subAutomaton = targetInfo.sub,
                        name = refNt.value,
                        bindName = false,
                        registerSubManager = true,
                        visibleInParent = true,
                        position = Point2D.ZERO
                    )
                }
            }
        }
    }

    private fun createBoundaryStates(automatons: List<RecursiveAutomaton>): Pair<Map<RecursiveAutomaton, State>, Map<RecursiveAutomaton, State>> {
        val start = mutableMapOf<RecursiveAutomaton, State>()
        val end = mutableMapOf<RecursiveAutomaton, State>()
        automatons.forEach { sub ->
            start[sub] = sub.addState().apply { isInitial = true }
            end[sub] = sub.addState().apply { isFinal = true }
        }
        return start to end
    }

    private fun buildRegexIntoAutomaton(
        automaton: RecursiveAutomaton, regex: RARegex?, start: State, end: State
    ) {
        when (regex) {
            null, RARegex.Eps -> addEpsilon(automaton, start, end)
            is RARegex.Terminal -> addTerminal(automaton, start, end, regex.ch)
            is RARegex.NonTerminalRef -> addNonterminalRef(automaton, start, end, regex.name)
            is RARegex.Concat -> {
                val mid = automaton.addState()
                buildRegexIntoAutomaton(automaton, regex.left, start, mid)
                buildRegexIntoAutomaton(automaton, regex.right, mid, end)
            }

            is RARegex.Alt -> buildAlternation(automaton, start, end, regex.a, regex.b)
            is RARegex.KleeneStar -> buildKleeneStar(automaton, start, end, regex.inner)
        }
    }


    private fun addEpsilon(automaton: RecursiveAutomaton, from: State, to: State) =
        setRegex(automaton, automaton.addTransition(from, to), EPSILON_VALUE)

    private fun addTerminal(automaton: RecursiveAutomaton, from: State, to: State, ch: Char) =
        setRegex(automaton, automaton.addTransition(from, to), FormalRegex.Singleton(ch))

    private fun addNonterminalRef(automaton: RecursiveAutomaton, from: State, to: State, name: String) {
        val targetBox = automaton.vertices.filterIsInstance<RecursiveAutomatonBox>().firstOrNull { it.name == name }
        if (targetBox != null) {
            addEpsilon(automaton, from, targetBox)
            setRegex(automaton, automaton.addTransition(targetBox, to), EPSILON_VALUE)
        } else {
            addEpsilon(automaton, from, to)
        }
    }

    private fun addEpsilon(
        automaton: RecursiveAutomaton,
        from: automaton.constructor.model.element.AutomatonVertex,
        to: automaton.constructor.model.element.AutomatonVertex
    ) = setRegex(automaton, automaton.addTransition(from, to), EPSILON_VALUE)

    private fun buildAlternation(
        automaton: RecursiveAutomaton, start: State, end: State, a: RARegex?, b: RARegex?
    ) {
        val aIsEps = a == null || a is RARegex.Eps
        val bIsEps = b == null || b is RARegex.Eps
        if (aIsEps || bIsEps) addEpsilon(automaton, start, end)
        if (!aIsEps) buildRegexIntoAutomaton(automaton, a, start, end)
        if (!bIsEps) buildRegexIntoAutomaton(automaton, b, start, end)
    }

    private fun buildKleeneStar(automaton: RecursiveAutomaton, start: State, end: State, inner: RARegex?) {
        if (inner == null || inner is RARegex.Eps) {
            addEpsilon(automaton, start, end)
            return
        }
        val mid = automaton.addState()
        addEpsilon(automaton, start, mid)
        buildRegexIntoAutomaton(automaton, inner, mid, mid)
        addEpsilon(automaton, mid, end)
    }


    private fun parseRegex(rhs: String): RARegex? = try {
        RARegex.parse(rhs)
    } catch (e: Exception) {
        throw e
    }

    private fun normalizeRegex(r: RARegex?): RARegex? = when (r) {
        null -> null
        RARegex.Eps -> RARegex.Eps
        is RARegex.Terminal -> if (r.ch == '$') RARegex.Eps else r
        is RARegex.NonTerminalRef -> r
        is RARegex.Concat -> RARegex.Concat(normalizeRegex(r.left), normalizeRegex(r.right))
        is RARegex.Alt -> RARegex.Alt(normalizeRegex(r.a), normalizeRegex(r.b))
        is RARegex.KleeneStar -> RARegex.KleeneStar(normalizeRegex(r.inner))
    }

    private fun collectReferencedNonterminals(r: RARegex?): MutableSet<String> {
        val acc = mutableSetOf<String>()
        fun visit(node: RARegex?) {
            when (node) {
                null, RARegex.Eps, is RARegex.Terminal -> {}
                is RARegex.NonTerminalRef -> acc.add(node.name)
                is RARegex.Concat -> {
                    visit(node.left); visit(node.right)
                }

                is RARegex.Alt -> {
                    visit(node.a); visit(node.b)
                }

                is RARegex.KleeneStar -> visit(node.inner)
            }
        }
        visit(r)
        return acc
    }


    private fun collectReachableAutomata(start: RecursiveAutomaton): MutableSet<RecursiveAutomaton> {
        val acc = mutableSetOf<RecursiveAutomaton>()
        fun dfs(cur: RecursiveAutomaton) {
            if (!acc.add(cur)) return
            cur.vertices.filterIsInstance<RecursiveAutomatonBox>().forEach { box ->
                val sub = box.subAutomaton
                if (sub !== cur && sub is RecursiveAutomaton) dfs(sub)
            }
        }
        dfs(start)
        return acc
    }

    private fun applyElkLayout(automaton: RecursiveAutomaton) {
        val bounds = automaton.transitions.associateWith { BoundingBox(0.0, 0.0, 40.0, 15.0) }
        val mapping = automaton.toElkGraphMapping(bounds)
        ELKLayeredLayout.configureLayout(mapping.elkGraph)
        RecursiveGraphLayoutEngine().layout(mapping.elkGraph, BasicProgressMonitor())
        automaton.applyLayout(mapping, bounds)
    }


    private fun eliminateImmediateLeftRecursion(original: EBNFGrammar): EBNFGrammar {
        val g = EBNFGrammar()
        val existing = original.productions.groupBy { it.leftSide }
        // Copy terminals & productions
        original.productions.forEach {
            g.addNonterminal(it.leftSide)
            g.addProduction(it.leftSide, it.rightSide)
        }
        g.initialNonterminal = original.initialNonterminal

        val used = original.productions.map { it.leftSide.value }.toMutableSet()
        fun fresh(): Nonterminal? {
            for (c in 'A'..'Z') if (c.toString() !in used) return Nonterminal(c.toString()).also { used.add(it.value) }
            return null
        }

        val toAdd = mutableListOf<Pair<Nonterminal, String>>()
        val toRemove = mutableListOf<Pair<Nonterminal, String>>()

        fun concatTokens(a: String, b: String): String {
            if (a.isEmpty()) return b
            if (b.isEmpty()) return a
            val needSpace = a.last().isLetterOrDigit() && b.first().isLetterOrDigit()
            return if (needSpace) "$a $b" else a + b
        }

        existing.forEach { (nt, prods) ->
            val leftRecFull = mutableListOf<String>()      // full productions with immediate left recursion
            val leftRecAlpha = mutableListOf<String>()     // the alpha parts after the left nonterminal
            val nonLeft = mutableListOf<String>()          // productions without immediate left recursion
            prods.forEach { p ->
                val rs = p.rightSide
                val trimmedNt = nt.value
                val isPrefixed = rs.startsWith(trimmedNt)
                val boundaryOk =
                    rs.length > trimmedNt.length && (rs.length == trimmedNt.length + 1 || rs[trimmedNt.length].isWhitespace() || !rs[trimmedNt.length].isLetterOrDigit())
                if (isPrefixed && boundaryOk) {
                    leftRecFull.add(p.rightSide)
                    leftRecAlpha.add(rs.drop(trimmedNt.length).trimStart())
                } else {
                    nonLeft.add(p.rightSide)
                }
            }
            if (leftRecAlpha.isNotEmpty() && nonLeft.isNotEmpty()) {
                val newNt = fresh() ?: return g // out of fresh letters -> return partially transformed grammar
                leftRecFull.forEach { lr -> toRemove.add(nt to lr) }
                nonLeft.forEach { beta -> toRemove.add(nt to beta) }
                nonLeft.forEach { beta -> toAdd.add(nt to concatTokens(beta.trimEnd(), newNt.value)) }
                leftRecAlpha.forEach { alpha -> toAdd.add(newNt to concatTokens(alpha.trimEnd(), newNt.value)) }
                toAdd.add(newNt to "$") // epsilon
            }
        }

        toRemove.forEach { (l, r) -> g.productions.removeIf { it.leftSide == l && it.rightSide == r } }
        toAdd.forEach { (l, r) -> g.addProduction(l, r) }
        return g
    }

    private fun setRegex(
        automaton: RecursiveAutomaton, t: automaton.constructor.model.element.Transition, r: FormalRegex?
    ) {
        t.getProperty(automaton.inputTape.expectedChar).set(r)
    }
}
