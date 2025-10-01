package automaton.constructor.model.grammar

class EBNFProduction(
    override val leftSide: Nonterminal, override val rightSide: RARegex
) : ProductionInterface<RARegex>

class EBNFGrammar(var newInitialNonterminal: Nonterminal? = null) : Grammar<RARegex> {
    var declaredInitialName: String? = null
    override val productions: MutableList<ProductionInterface<RARegex>> = mutableListOf()
    override val nonterminals: MutableList<Nonterminal> = mutableListOf()
    override lateinit var initialNonterminal: Nonterminal

    private var nonterminalsCount = 0
    private val nonterminalsValues = mutableSetOf<String>()

    fun addNonterminal(value: String = "A"): Nonterminal {
        val newNt = if (nonterminalsValues.contains(value)) Nonterminal(value + nonterminalsCount)
        else Nonterminal(value)
        nonterminals.add(newNt)
        nonterminalsValues.add(newNt.value)
        nonterminalsCount++
        return newNt
    }

    fun addNonterminal(nonterminal: Nonterminal): Boolean {
        if (nonterminalsValues.contains(nonterminal.value)) return false
        nonterminals.add(nonterminal)
        nonterminalsValues.add(nonterminal.value)
        nonterminalsCount++
        return true
    }

    fun findOrAddNonterminal(value: String): Nonterminal =
        nonterminals.firstOrNull { it.value == value } ?: addNonterminal(value)

    fun addProduction(leftSide: Nonterminal, rightSide: RARegex) {
        productions.add(EBNFProduction(leftSide, rightSide))
    }

    fun clearProductions() = productions.clear()
}