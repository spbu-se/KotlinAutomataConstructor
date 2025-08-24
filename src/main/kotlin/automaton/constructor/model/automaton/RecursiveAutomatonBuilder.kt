package automaton.constructor.model.automaton

import automaton.constructor.model.grammar.EBNFGrammar
import automaton.constructor.model.grammar.Nonterminal
import automaton.constructor.model.element.RecursiveAutomatonBox
import automaton.constructor.model.element.State
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
 * Utility responsible for converting a context-free grammar (in EBNF) into
 * a RecursiveAutomaton structure.
 */
object RecursiveAutomatonBuilder {
    private fun setRegex(
        automaton: RecursiveAutomaton, t: automaton.constructor.model.element.Transition, r: FormalRegex?
    ) {
        t.getProperty(automaton.inputTape.expectedChar).set(r)
    }

    private fun eliminateImmediateLeftRecursion(original: EBNFGrammar): EBNFGrammar {
        val g = EBNFGrammar()
        val existing = original.productions.groupBy { it.leftSide }
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
            val leftRecFull = mutableListOf<String>()
            val leftRecAlpha = mutableListOf<String>()
            val nonLeft = mutableListOf<String>()
            prods.forEach { p ->
                val rs = p.rightSide
                val trimmedNt = nt.value
                if (rs.startsWith(trimmedNt) && rs.length > trimmedNt.length && (rs.length == trimmedNt.length + 1 || rs[trimmedNt.length].isWhitespace() || !rs[trimmedNt.length].isLetterOrDigit())) {
                    leftRecFull.add(p.rightSide)
                    leftRecAlpha.add(rs.drop(trimmedNt.length).trimStart())
                } else nonLeft.add(p.rightSide)
            }
            if (leftRecAlpha.isNotEmpty() && nonLeft.isNotEmpty()) {
                val newNt = fresh() ?: return g
                leftRecFull.forEach { lr -> toRemove.add(nt to lr) }
                nonLeft.forEach { beta -> toRemove.add(nt to beta) }
                nonLeft.forEach { beta -> toAdd.add(nt to concatTokens(beta.trimEnd(), newNt.value)) }
                leftRecAlpha.forEach { alpha -> toAdd.add(newNt to concatTokens(alpha.trimEnd(), newNt.value)) }
                toAdd.add(newNt to "$")
            }
        }
        toRemove.forEach { (l, r) -> g.productions.removeIf { it.leftSide == l && it.rightSide == r } }
        toAdd.forEach { (l, r) -> g.addProduction(l, r) }
        return g
    }

    fun buildFromGrammar(root: RecursiveAutomaton, grammar: EBNFGrammar) {
        fun collectReachable(start: RecursiveAutomaton, acc: MutableSet<RecursiveAutomaton>) {
            if (!acc.add(start)) return
            start.vertices.filterIsInstance<RecursiveAutomatonBox>().forEach { box ->
                val sub = box.subAutomaton
                if (sub !== start && sub is RecursiveAutomaton) collectReachable(sub, acc)
            }
        }

        val previouslyReachable = mutableSetOf<RecursiveAutomaton>()
        collectReachable(root, previouslyReachable)
        previouslyReachable.remove(root)
        root.vertices.toList().forEach { root.removeVertex(it) }
        root.nameProperty.unbind()
        if (grammar.productions.isEmpty()) return
        val transformed = eliminateImmediateLeftRecursion(grammar)
        val initial = transformed.initialNonterminal
        val grouped = transformed.productions.groupBy { it.leftSide }
            .mapValues { (_, ps) -> ps.joinToString("|") { it.rightSide.trim() } }
        val parsedRaw = grouped.mapValues { (_, rhs) ->
            try {
                RARegex.parse(rhs)
            } catch (e: Exception) {
                throw e
            }
        }

        if (root.name.isBlank() || root.name == root.untitledAdjective) {
            root.name = initial.value
        }
        fun normalize(r: RARegex?): RARegex? = when (r) {
            null -> null
            RARegex.Eps -> RARegex.Eps
            is RARegex.Terminal -> if (r.ch == '$') RARegex.Eps else r
            is RARegex.NonTerminalRef -> r
            is RARegex.Concat -> RARegex.Concat(normalize(r.left), normalize(r.right))
            is RARegex.Alt -> RARegex.Alt(normalize(r.a), normalize(r.b))
            is RARegex.KleeneStar -> RARegex.KleeneStar(normalize(r.inner))
        }

        val parsed = parsedRaw.mapValues { normalize(it.value) }
        if (initial !in grouped.keys) return

        data class Info(val nt: Nonterminal, val sub: RecursiveAutomaton)

        val infos = mutableMapOf<Nonterminal, Info>()
        infos[initial] = Info(initial, root).also { root.name = initial.value }
        grouped.keys.filter { it != initial }.forEach { nt ->
            val sub = root.createEmptyAutomatonOfSameType()
            sub.name = nt.value
            infos[nt] = Info(nt, sub)
        }

        fun collectRefs(r: RARegex?, acc: MutableSet<String>) {
            when (r) {
                null, RARegex.Eps, is RARegex.Terminal -> {}; is RARegex.NonTerminalRef -> acc.add(r.name); is RARegex.Concat -> {
                collectRefs(r.left, acc); collectRefs(r.right, acc)
            }; is RARegex.Alt -> {
                collectRefs(r.a, acc); collectRefs(r.b, acc)
            }; is RARegex.KleeneStar -> collectRefs(r.inner, acc)
            }
        }

        infos.values.forEach { info ->
            val refs = mutableSetOf<String>()
            collectRefs(parsed[info.nt], refs)
            if (info.nt.value in refs) refs.add(info.nt.value)
            refs.forEach { refName ->
                grouped.keys.firstOrNull { it.value == refName }?.let { refNt ->
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

        val startMap = mutableMapOf<RecursiveAutomaton, State>()
        val endMap = mutableMapOf<RecursiveAutomaton, State>()
        infos.values.forEach { (_, sub) ->
            startMap[sub] = sub.addState().apply { isInitial = true }
            endMap[sub] = sub.addState().apply { isFinal = true }
        }

        fun buildRARegex(
            currentNt: Nonterminal, automaton: RecursiveAutomaton, regex: RARegex?, start: State, end: State
        ) {
            when (regex) {
                null, RARegex.Eps -> setRegex(automaton, automaton.addTransition(start, end), EPSILON_VALUE)
                is RARegex.Terminal -> setRegex(
                    automaton, automaton.addTransition(start, end), FormalRegex.Singleton(regex.ch)
                )

                is RARegex.NonTerminalRef -> {
                    val targetBox = automaton.vertices.filterIsInstance<RecursiveAutomatonBox>()
                        .firstOrNull { it.name == regex.name }
                    if (targetBox != null) {
                        setRegex(automaton, automaton.addTransition(start, targetBox), EPSILON_VALUE)
                        setRegex(automaton, automaton.addTransition(targetBox, end), EPSILON_VALUE)
                    } else {
                        setRegex(automaton, automaton.addTransition(start, end), EPSILON_VALUE)
                    }
                }

                is RARegex.Concat -> {
                    val mid = automaton.addState() // auto-named
                    buildRARegex(currentNt, automaton, regex.left, start, mid)
                    buildRARegex(currentNt, automaton, regex.right, mid, end)
                }

                is RARegex.Alt -> {
                    val a = regex.a
                    val b = regex.b

                    if (a == null || a is RARegex.Eps) {
                        setRegex(automaton, automaton.addTransition(start, end), EPSILON_VALUE)
                        if (!(b == null || b is RARegex.Eps)) buildRARegex(currentNt, automaton, b, start, end)
                    } else if (b == null || b is RARegex.Eps) {
                        setRegex(automaton, automaton.addTransition(start, end), EPSILON_VALUE)
                        buildRARegex(currentNt, automaton, a, start, end)
                    } else {
                        // both non-eps: just build both branches between the same start and end
                        buildRARegex(currentNt, automaton, a, start, end)
                        buildRARegex(currentNt, automaton, b, start, end)
                    }
                }

                is RARegex.KleeneStar -> {
                    val inner = regex.inner
                    if (inner == null || inner is RARegex.Eps) {
                        setRegex(automaton, automaton.addTransition(start, end), EPSILON_VALUE)
                    } else {
                        val mid = automaton.addState()
                        setRegex(automaton, automaton.addTransition(start, mid), EPSILON_VALUE)
                        buildRARegex(currentNt, automaton, inner, mid, mid)
                        setRegex(automaton, automaton.addTransition(mid, end), EPSILON_VALUE)
                    }

                }
            }
        }

        infos.values.forEach { (nt, sub) ->
            val s = startMap.getValue(sub)
            val f = endMap.getValue(sub)
            buildRARegex(nt, sub, parsed[nt], s, f)
        }

        infos.values.map { it.sub }.distinct().forEach { applyElkLayout(it) }

        val nowReachable = mutableSetOf<RecursiveAutomaton>()
        collectReachable(root, nowReachable)
        nowReachable.remove(root)
        val toRemove = previouslyReachable - nowReachable
        toRemove.forEach { old ->
            if (old.referenceCount == 0) {
                RecursiveAutomaton.registry.remove(old)
            }
        }
    }

    private fun applyElkLayout(automaton: RecursiveAutomaton) {
        val bounds = automaton.transitions.associateWith { BoundingBox(0.0, 0.0, 40.0, 15.0) }
        val mapping = automaton.toElkGraphMapping(bounds)
        ELKLayeredLayout.configureLayout(mapping.elkGraph)
        RecursiveGraphLayoutEngine().layout(mapping.elkGraph, BasicProgressMonitor())
        automaton.applyLayout(mapping, bounds)
    }
}
