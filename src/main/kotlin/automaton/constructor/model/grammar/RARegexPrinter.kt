package automaton.constructor.model.grammar

fun RARegex.render(): String = when (this) {
    RARegex.Eps -> "$"
    is RARegex.Terminal -> {
        ch.toString()
    }

    is RARegex.NonTerminalRef -> name
    is RARegex.Concat -> {
        val parts = flattenConcat().map { part ->
            val needsParens = part is RARegex.Alt
            if (needsParens) "(${part.render()})" else part.render()
        }
        parts.joinToString(" ")
    }

    is RARegex.Alt -> flattenAlt().joinToString(" | ") { alt ->
        val needsParens = alt is RARegex.Alt
        if (needsParens) "(${alt.render()})" else alt.render()
    }

    is RARegex.KleeneStar -> {
        val innerStr = when (inner) {
            null, RARegex.Eps -> "$"
            is RARegex.Alt -> "(${inner.render()})"
            else -> inner.render()
        }
        "$innerStr*"
    }
}

private fun RARegex.Concat.flattenConcat(): List<RARegex> {
    fun collect(node: RARegex?, acc: MutableList<RARegex>) {
        when (node) {
            null, RARegex.Eps -> {}
            is RARegex.Concat -> {
                collect(node.left, acc)
                collect(node.right, acc)
            }

            else -> acc += node
        }
    }
    return buildList { collect(this@flattenConcat, this) }
}

private fun RARegex.Alt.flattenAlt(): List<RARegex> {
    fun collect(node: RARegex?, acc: MutableList<RARegex>) {
        when (node) {
            null, RARegex.Eps -> acc += RARegex.Eps
            is RARegex.Alt -> {
                collect(node.a, acc)
                collect(node.b, acc)
            }

            else -> acc += node
        }
    }
    return buildList { collect(this@flattenAlt, this) }
}