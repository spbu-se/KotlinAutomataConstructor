package automaton.constructor.model.automaton

import automaton.constructor.model.element.AutomatonVertex
import automaton.constructor.model.element.RecursiveAutomatonBox
import automaton.constructor.model.element.State
import automaton.constructor.model.grammar.EBNFGrammar
import automaton.constructor.model.grammar.EBNFProduction
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

        val normalized = grouped.mapValues { (_, r) -> normalizeRegex(r) }

        val infos = createAutomataForNonterminals(root, grouped.keys, initial)

        addReferenceBoxes(infos, grouped.keys, normalized)

        val (startStates, endStates) = createBoundaryStates(infos.values.map { it.sub })

        infos.values.forEach { (nt, sub) ->
            buildRegexIntoAutomaton(sub, normalized[nt], startStates.getValue(sub), endStates.getValue(sub))
        }

        infos.values.map { it.sub }.distinct().forEach { applyElkLayout(it) }

        val nowReachable = collectReachableAutomata(root).apply { remove(root) }
        val toRemove = previouslyReachable - nowReachable
        toRemove.forEach { old -> if (old.referenceCount == 0) RecursiveAutomaton.registry.remove(old) }
    }

    private data class Info(val nt: Nonterminal, val sub: RecursiveAutomaton)

    private fun groupProductions(g: EBNFGrammar): Map<Nonterminal, RARegex?> =
        g.productions.groupBy { it.leftSide }.mapValues { (_, ps) ->
            ps.map { it.rightSide }.reduceOrNull { acc, r -> RARegex.Alt(acc, r) }
        }

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
                // Recursively chain left/right
                val nodes = flattenConcat(regex)
                var current = start
                nodes.forEachIndexed { idx, piece ->
                    val target = if (idx == nodes.lastIndex) end else automaton.addState()
                    buildRegexIntoAutomaton(automaton, piece, current, target)
                    current = target
                }
            }

            is RARegex.Alt -> {
                val alts = flattenAlt(regex)
                alts.forEach { alt -> buildRegexIntoAutomaton(automaton, alt, start, end) }
            }

            is RARegex.KleeneStar -> {
                val inner = regex.inner
                if (inner == null || inner is RARegex.Eps) {
                    addEpsilon(automaton, start, end)
                } else {
                    val mid = automaton.addState()
                    addEpsilon(automaton, start, mid)
                    buildRegexIntoAutomaton(automaton, inner, mid, mid)
                    addEpsilon(automaton, mid, end)
                    addEpsilon(automaton, start, end)
                }
            }
        }
    }

    private fun flattenConcat(c: RARegex.Concat): List<RARegex> {
        val acc = mutableListOf<RARegex>()
        fun visit(r: RARegex?) {
            when (r) {
                null, RARegex.Eps -> {}
                is RARegex.Concat -> {
                    visit(r.left); visit(r.right)
                }

                else -> acc += r
            }
        }
        visit(c)
        return acc
    }

    private fun flattenAlt(a: RARegex.Alt): List<RARegex> {
        val acc = mutableListOf<RARegex>()
        fun visit(r: RARegex?) {
            when (r) {
                null -> {}
                is RARegex.Alt -> {
                    visit(r.a); visit(r.b)
                }

                else -> acc += r
            }
        }
        visit(a)
        return acc
    }

    private fun addEpsilon(automaton: RecursiveAutomaton, from: State, to: State) =
        setRegex(automaton, automaton.addTransition(from, to), EPSILON_VALUE)

    private fun addEpsilon(
        automaton: RecursiveAutomaton, from: AutomatonVertex, to: AutomatonVertex
    ) = setRegex(automaton, automaton.addTransition(from, to), EPSILON_VALUE)

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

    private fun normalizeRegex(r: RARegex?): RARegex? = when (r) {
        null -> null
        RARegex.Eps -> RARegex.Eps
        is RARegex.Terminal -> if (r.ch == '$') RARegex.Eps else r
        is RARegex.NonTerminalRef -> r
        is RARegex.Concat -> {
            val left = normalizeRegex(r.left)
            val right = normalizeRegex(r.right)
            when {
                left == null || left == RARegex.Eps -> right
                right == null || right == RARegex.Eps -> left
                else -> RARegex.Concat(left, right)
            }
        }

        is RARegex.Alt -> {
            val a = normalizeRegex(r.a)
            val b = normalizeRegex(r.b)
            when {
                a == null -> b
                b == null -> a
                else -> RARegex.Alt(a, b)
            }
        }

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
        original.nonterminals.forEach { g.addNonterminal(it) }
        g.initialNonterminal = original.initialNonterminal

        original.productions.forEach { g.addProduction(it.leftSide, it.rightSide) }

        val byNt = g.productions.groupBy { it.leftSide }.toMutableMap()

        val newProductions = mutableListOf<EBNFProduction>()
        val toRemove = mutableSetOf<EBNFProduction>()

        fun startsWith(nt: Nonterminal, r: RARegex): Pair<Boolean, RARegex?> {
            return when (r) {
                is RARegex.NonTerminalRef -> if (r.name == nt.value) true to RARegex.Eps else false to null
                is RARegex.Concat -> {
                    when (val l = r.left) {
                        is RARegex.NonTerminalRef -> if (l.name == nt.value) true to r.right else false to null
                        RARegex.Eps -> startsWith(nt, r.right ?: RARegex.Eps)
                        else -> false to null
                    }
                }

                else -> false to null
            }
        }

        fun concat(a: RARegex?, b: RARegex?): RARegex? {
            return when {
                a == null || a == RARegex.Eps -> b
                b == null || b == RARegex.Eps -> a
                else -> RARegex.Concat(a, b)
            }
        }

        val usedNames = g.nonterminals.map { it.value }.toMutableSet()
        fun fresh(): Nonterminal? {
            for (c in 'A'..'Z') {
                val name = c.toString()
                if (name !in usedNames) {
                    usedNames += name
                    val nt = Nonterminal(name)
                    g.addNonterminal(nt)
                    return nt
                }
            }
            return null
        }

        byNt.forEach { (nt, prods) ->
            val recursiveAlphas = mutableListOf<RARegex?>()
            val betas = mutableListOf<RARegex?>()
            prods.forEach { p ->
                val (isRec, remainder) = startsWith(nt, p.rightSide)
                if (isRec) {
                    recursiveAlphas += remainder
                    toRemove += (p as EBNFProduction)
                } else {
                    betas += p.rightSide
                }
            }
            if (recursiveAlphas.isNotEmpty() && betas.isNotEmpty()) {
                val ntPrime = fresh() ?: return g
                betas.forEach { beta ->
                    val newRhs =
                        concat(beta, RARegex.NonTerminalRef(ntPrime.value)) ?: RARegex.NonTerminalRef(ntPrime.value)
                    newProductions += EBNFProduction(nt, newRhs)
                }
                recursiveAlphas.forEach { alpha ->
                    val newRhs =
                        concat(alpha, RARegex.NonTerminalRef(ntPrime.value)) ?: RARegex.NonTerminalRef(ntPrime.value)
                    newProductions += EBNFProduction(ntPrime, newRhs)
                }
                newProductions += EBNFProduction(ntPrime, RARegex.Eps)
            }
        }

        g.productions.removeAll(toRemove)
        g.productions.addAll(newProductions)

        return g
    }

    private fun setRegex(
        automaton: RecursiveAutomaton, t: automaton.constructor.model.element.Transition, r: FormalRegex?
    ) {
        t.getProperty(automaton.inputTape.expectedChar).set(r)
    }
}