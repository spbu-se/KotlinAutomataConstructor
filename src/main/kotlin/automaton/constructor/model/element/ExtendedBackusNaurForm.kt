package automaton.constructor.model.element

import javafx.beans.property.SimpleStringProperty
import tornadofx.*

interface EBNFSymbol {
    fun getSymbol(): String
}

class EBNFProduction(
    leftSide: String = "",
    rightSide: String = ""
) {
    val leftSideProperty = SimpleStringProperty(leftSide)
    var leftSide by leftSideProperty

    val rightSideProperty = SimpleStringProperty(rightSide)
    var rightSide by rightSideProperty
}

data class EBNFGrammar(
    val initialNonterminal: String,
    val productions: List<EBNFProduction>
)