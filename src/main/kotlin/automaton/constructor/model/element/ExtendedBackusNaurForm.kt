package automaton.constructor.model.element

class EBNFSymbol(var value: String) : CFGSymbol {
    override fun getSymbol(): String = value
}

class EBNFProduction(
    override val leftSide: Nonterminal,
    override val rightSide: String
) : ProductionInterface<String> {
    override fun toString(): String {
        return super.toString()
    }
}

data class EBNFGrammar(var newInitialNonterminal: Nonterminal? = null) : Grammar<String> {
    override val productions: MutableList<ProductionInterface<String>> = mutableListOf()
    override val nonterminals: MutableList<Nonterminal> = mutableListOf()
    override lateinit var initialNonterminal: Nonterminal

    //    TODO: Implement methods for EBNF grammar handling
    fun addProduction(leftSide: Nonterminal, rightSide: String) {
        productions.add(EBNFProduction(leftSide, rightSide))
    }


}