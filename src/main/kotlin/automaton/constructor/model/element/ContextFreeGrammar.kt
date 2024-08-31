package automaton.constructor.model.element

interface CFGSymbol {
    fun getSymbol(): String
}
class Terminal(var value: Char): CFGSymbol {
    override fun getSymbol() = value.toString()
}
class Nonterminal(var value: String): CFGSymbol {
    override fun getSymbol() = value
}
class Production(val leftSide: Nonterminal, val rightSide: MutableList<CFGSymbol>) {
    override fun toString() = leftSide.value + ";" + rightSide.joinToString(separator = ",") { it.getSymbol() }
}

class ContextFreeGrammar(newInitialNonterminal: Nonterminal? = null) {
    val nonterminals = mutableListOf<Nonterminal>()
    val productions = mutableListOf<Production>()
    private var nonterminalsCount = 0 // count of all ever used nonterminals, used for naming to avoid collisions
    private val nonterminalsValues = mutableSetOf<String>()
    var initialNonterminal: Nonterminal

    init {
        initialNonterminal = if (newInitialNonterminal == null) {
            addNonterminal("S")
        } else {
            addNonterminal(newInitialNonterminal)
            newInitialNonterminal
        }
    }

    fun addNonterminal(value: String = "A"): Nonterminal {
        val newNonterminal = if (nonterminalsValues.contains(value)) {
            Nonterminal(value + nonterminalsCount.toString())
        } else {
            Nonterminal(value)
        }
        nonterminals.add(newNonterminal)
        nonterminalsCount++
        nonterminalsValues.add(newNonterminal.value)
        return newNonterminal
    }

    fun addNonterminal(nonterminal: Nonterminal): Boolean {
        if (nonterminalsValues.contains(nonterminal.value))
            return false
        nonterminals.add(nonterminal)
        nonterminalsCount++
        nonterminalsValues.add(nonterminal.value)
        return true
    }

    fun removeNonterminal(nonterminal: Nonterminal) {
        if (nonterminal == initialNonterminal) {
            return
        }
        nonterminals.remove(nonterminal)
        nonterminalsValues.remove(nonterminal.value)
    }

    private fun removeEpsilonProductions() {
        var areThereNullableNonterminals = true
        val nulledNonterminals = mutableSetOf<Nonterminal>()
        while (areThereNullableNonterminals) {
            val nullableNonterminals = mutableSetOf<Nonterminal>()
            productions.forEach {
                if (it.rightSide.isEmpty() && it.leftSide != initialNonterminal && !nulledNonterminals.contains(it.leftSide)) {
                    nullableNonterminals.add(it.leftSide)
                    nulledNonterminals.add(it.leftSide)
                }
            }
            productions.removeAll { it.rightSide.isEmpty() && it.leftSide != initialNonterminal }
            nullableNonterminals.forEach { nonterminal ->
                val productionsToAdd = mutableListOf<Production>()
                productions.forEach {
                    fun addProductions(production: Production, start: Int) {
                        for (i in start..production.rightSide.lastIndex) {
                            if (production.rightSide[i] == nonterminal) {
                                val newRightSide = production.rightSide.toMutableList()
                                newRightSide.removeAt(i)
                                val newProduction = Production(production.leftSide, newRightSide)
                                if (newRightSide.isNotEmpty()) {
                                    productionsToAdd.add(newProduction)
                                    addProductions(newProduction, i)
                                } else if (newProduction.leftSide != nonterminal) {
                                    productionsToAdd.add(newProduction)
                                }
                            }
                        }
                    }

                    addProductions(it, 0)
                }
                productions.addAll(productionsToAdd)
            }
            areThereNullableNonterminals = nullableNonterminals.isNotEmpty()
        }
    }

    private fun removeUnitProductions() {
        val unitProductions = mutableMapOf<Nonterminal, MutableSet<Nonterminal>>()
        val productionsByNonterminals = mutableMapOf<Nonterminal, MutableSet<MutableList<CFGSymbol>>>()
        productions.forEach {
            if (it.rightSide.size == 1 && it.rightSide[0] is Nonterminal) {
                if (it.leftSide != it.rightSide[0]) {
                    if (!unitProductions.containsKey(it.leftSide)) {
                        unitProductions[it.leftSide] = mutableSetOf()
                    }
                    unitProductions[it.leftSide]!!.add(it.rightSide[0] as Nonterminal)
                }
            } else {
                if (!productionsByNonterminals.containsKey(it.leftSide)) {
                    productionsByNonterminals[it.leftSide] = mutableSetOf()
                }
                productionsByNonterminals[it.leftSide]!!.add(it.rightSide)
            }
        }
        productions.removeAll { it.rightSide.size == 1 && it.rightSide[0] is Nonterminal }
        unitProductions.keys.forEach { leftNonterminal ->
            var rightSidesOfNewProductions = unitProductions[leftNonterminal]!!
            while (rightSidesOfNewProductions.isNotEmpty()) {
                val newRightSides = mutableSetOf<Nonterminal>()
                rightSidesOfNewProductions.forEach { rightNonterminal ->
                    productionsByNonterminals[rightNonterminal]?.forEach {
                        if (!productionsByNonterminals.containsKey(leftNonterminal)) {
                            productionsByNonterminals[leftNonterminal] = mutableSetOf()
                        }
                        if (!productionsByNonterminals[leftNonterminal]!!.contains(it)) {
                            productionsByNonterminals[leftNonterminal]!!.add(it)
                            productions.add(Production(leftNonterminal, it))
                        }
                    }
                    if (unitProductions.containsKey(rightNonterminal)) {
                        newRightSides.addAll(unitProductions[rightNonterminal]!!)
                    }
                }
                newRightSides.remove(leftNonterminal)
                newRightSides.removeAll { unitProductions[leftNonterminal]!!.contains(it) }
                rightSidesOfNewProductions = newRightSides
            }
        }
    }

    private fun removeMixOfTerminalsAndNonterminals() {
        val newNonterminals = mutableMapOf<Char, Nonterminal>()
        val productionsToAdd = mutableListOf<Production>()
        productions.forEach {
            if (!(it.rightSide.size == 1 && it.rightSide[0] is Terminal || it.rightSide.isEmpty())) {
                for (i in it.rightSide.indices) {
                    if (it.rightSide[i] is Terminal) {
                        var replacementNonterminal = newNonterminals[it.rightSide[i].getSymbol()[0]]
                        if (replacementNonterminal == null) {
                            replacementNonterminal = addNonterminal("U")
                            newNonterminals[it.rightSide[i].getSymbol()[0]] = replacementNonterminal
                            productionsToAdd.add(Production(replacementNonterminal, mutableListOf(it.rightSide[i])))
                        }
                        it.rightSide[i] = replacementNonterminal
                    }
                }
            }
        }
        productions.addAll(productionsToAdd)
    }

    private fun removeLongRightSides() {
        val productionsToAdd = mutableListOf<Production>()
        productions.forEach {
            if (it.rightSide.size > 2) {
                while (it.rightSide.size > 2) {
                    val newNonterminal = addNonterminal("Y")
                    productionsToAdd.add(Production(newNonterminal, mutableListOf(it.rightSide[0], it.rightSide[1])))
                    it.rightSide.removeFirst()
                    it.rightSide[0] = newNonterminal
                }
            }
        }
        productions.addAll(productionsToAdd)
    }

    fun removeInitialNonterminalFromRightSides() {
        val isThereInitialNonterminalInRightSide = productions.any { production ->
            production.rightSide.any { it == initialNonterminal }
        }
        if (isThereInitialNonterminalInRightSide) {
            val newInitialNonterminal = addNonterminal("S")
            productions.add(Production(newInitialNonterminal, mutableListOf(initialNonterminal)))
            initialNonterminal = newInitialNonterminal
        }
    }

    fun convertToCNF() {
        removeInitialNonterminalFromRightSides()
        removeEpsilonProductions()
        removeUnitProductions()
        removeMixOfTerminalsAndNonterminals()
        removeLongRightSides()
    }

    private fun removeUnreachableNonterminals() {
        val productionsByNonterminals = mutableMapOf<Nonterminal, MutableList<List<CFGSymbol>>>()
        productions.forEach {
            if (!productionsByNonterminals.containsKey(it.leftSide)) {
                productionsByNonterminals[it.leftSide] = mutableListOf()
            }
            productionsByNonterminals[it.leftSide]!!.add(it.rightSide)
        }
        val queue = ArrayDeque<Nonterminal>()
        queue.add(initialNonterminal)
        val reachableNonterminals = mutableSetOf(initialNonterminal)
        while (queue.isNotEmpty()) {
            val currentNonterminal = queue.removeFirst()
            productionsByNonterminals[currentNonterminal]?.forEach { rightSide ->
                rightSide.forEach {
                    if (it is Nonterminal && !reachableNonterminals.contains(it)) {
                        queue.add(it)
                        reachableNonterminals.add(it)
                    }
                }
            }
        }

        nonterminals.filter { !reachableNonterminals.contains(it) }.forEach { removeNonterminal(it) }
        productions.filter { production ->
            var areAllNonterminalsReachable = reachableNonterminals.contains(production.leftSide)
            production.rightSide.forEach {
                if (it is Nonterminal && !reachableNonterminals.contains(it))
                    areAllNonterminalsReachable = false
            }
            !areAllNonterminalsReachable
        }.forEach { productions.remove(it) }
    }

    private fun removeNonconvertibleNonterminals() {
        val convertibleIntoTerminals = mutableSetOf<Nonterminal>()
        productions.forEach { production ->
            if (production.rightSide.all { it is Terminal }) {
                convertibleIntoTerminals.add(production.leftSide)
            }
        }
        var areThereNewConvertible = true
        while (areThereNewConvertible) {
            val newConvertible = productions.filter { production ->  
                var areAllNonterminalsConvertible = true
                production.rightSide.forEach {
                    if (it is Nonterminal && !convertibleIntoTerminals.contains(it)) {
                        areAllNonterminalsConvertible = false
                    }
                }
                areAllNonterminalsConvertible && !convertibleIntoTerminals.contains(production.leftSide)
            }.map { it.leftSide }.toList()
            convertibleIntoTerminals.addAll(newConvertible)
            areThereNewConvertible = newConvertible.isNotEmpty()
        }

        nonterminals.filter { !convertibleIntoTerminals.contains(it) }.forEach { removeNonterminal(it) }
        productions.filter { production ->
            var areAllNonterminalsConvertible = convertibleIntoTerminals.contains(production.leftSide)
            production.rightSide.forEach {
                if (it is Nonterminal && !convertibleIntoTerminals.contains(it))
                    areAllNonterminalsConvertible = false
            }
            !areAllNonterminalsConvertible
        }.forEach { productions.remove(it) }
    }

    fun removeUselessNonterminals() {
        removeNonconvertibleNonterminals()
        removeUnreachableNonterminals()
    }
}