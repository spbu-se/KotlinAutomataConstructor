package automaton.constructor.model.grammar

interface CFGSymbol {
    fun getSymbol(): String
}

interface Production<R> {
    val leftSide: Nonterminal
    val rightSide: R
}

class Terminal(var value: Char) : CFGSymbol {
    override fun getSymbol() = value.toString()
}

class Nonterminal(var value: String) : CFGSymbol {
    override fun getSymbol() = value
}

interface Grammar<R> {
    val productions: MutableList<Production<R>>
    val nonterminals: MutableList<Nonterminal>
    var initialNonterminal: Nonterminal
}