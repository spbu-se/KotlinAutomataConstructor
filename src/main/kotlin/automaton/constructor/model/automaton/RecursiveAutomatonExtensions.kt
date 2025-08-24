package automaton.constructor.model.automaton

fun RecursiveAutomaton.displayNameForMenu(): String {
    val g = grammar
    if (g != null) {
        val candidates = sequence {
            yield(g.declaredInitialName)
            yield(runCatching { g.initialNonterminal.value }.getOrNull())
            yield(g.newInitialNonterminal?.value)
            yield(g.nonterminals.firstOrNull()?.value)
            yield(g.productions.firstOrNull()?.leftSide?.value)
        }.filter { !it.isNullOrBlank() }.map { it!!.trim() }.toList()
        val chosen = candidates.firstOrNull { !it.startsWith("Untitled", ignoreCase = true) }
            ?: candidates.firstOrNull()
        if (!chosen.isNullOrBlank()) return chosen
    }
    return name
}

fun RecursiveAutomaton.allowedBoxes(): Set<RecursiveAutomaton> {
    val all = RecursiveAutomaton.allInstances()
    if (all.isEmpty()) return setOf(this)
    val forward = mutableMapOf<RecursiveAutomaton, MutableSet<RecursiveAutomaton>>()
    val indegree = mutableMapOf<RecursiveAutomaton, Int>().apply { all.forEach { this[it] = 0 } }
    all.forEach { ra ->
        val children = ra.vertices.filterIsInstance<automaton.constructor.model.element.RecursiveAutomatonBox>()
            .mapNotNull { it.subAutomaton as? RecursiveAutomaton }
            .filter { it !== ra }
        children.forEach { ch ->
            forward.getOrPut(ra) { mutableSetOf() }.add(ch)
            indegree[ch] = (indegree[ch] ?: 0) + 1
        }
    }
    val roots = indegree.filter { it.value == 0 }.keys.ifEmpty { setOf(this) }
    fun closure(start: RecursiveAutomaton): Set<RecursiveAutomaton> {
        val acc = mutableSetOf<RecursiveAutomaton>()
        fun dfs(cur: RecursiveAutomaton) {
            if (!acc.add(cur)) return
            forward[cur]?.forEach { dfs(it) }
        }
        dfs(start)
        return acc
    }

    val selectedRoot = roots.firstOrNull { this in closure(it) } ?: this
    val result = closure(selectedRoot)
    return (result + selectedRoot + this).toSet()
}