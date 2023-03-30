package automaton.constructor.model.property

import automaton.constructor.model.property.DynamicPropertyDescriptors.EPSILON_STRING
import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser

/**
 * Regular expression as it's formally defined in the text books using only:
 * - Concatenation
 * - Alternative
 * - Kleene star
 */
sealed class FormalRegex {
    data class Singleton(val char: Char) : FormalRegex()
    data class Concatenation(val left: FormalRegex?, val right: FormalRegex?) : FormalRegex()
    data class Alternative(val alternative1: FormalRegex?, val alternative2: FormalRegex?) : FormalRegex()
    data class KleeneStar(val repeated: FormalRegex?) : FormalRegex()
    companion object {
        val ESCAPABLE_CHARS = listOf('\\', '(', ')', '|', '*')

        private val regexGrammar = object : Grammar<FormalRegex?>() {
            val simpleChar by regexToken("""[^()|*\\]""")
            val escapedChar by regexToken("""\\[()|*\\]""")
            val lpar by literalToken("(")
            val rpar by literalToken(")")
            val bar by literalToken("|")
            val star by literalToken("*")

            val term: Parser<FormalRegex?> by (simpleChar use { Singleton(text.single()) }) or
                    (escapedChar use { Singleton(text.last()) }) or
                    (-lpar * parser(this::rootParser) * -rpar)

            val starTerm by (term * -oneOrMore(star)).map { KleeneStar(it) } or term
            val concatTerm: Parser<FormalRegex?> by zeroOrMore(starTerm).map { terms ->
                terms.reduceOrNull { acc, regex -> Concatenation(acc, regex) } ?: EPSILON_VALUE
            }
            override val rootParser by leftAssociative(concatTerm, bar) { l, _, r -> Alternative(l, r) }
        }

        fun fromString(string: String): FormalRegex? = regexGrammar.parseToEnd(string)
    }
}

fun FormalRegex?.toPrettyString(): String = when (this) {
    EPSILON_VALUE -> EPSILON_STRING
    is FormalRegex.Singleton -> when (char) {
        in FormalRegex.ESCAPABLE_CHARS -> "\\$char"
        else -> char.toString()
    }
    is FormalRegex.KleeneStar -> when (repeated) {
        EPSILON_VALUE -> ""
        is FormalRegex.Singleton -> "${repeated.toPrettyString()}*"
        else -> "(${repeated.toPrettyString()})*"
    }
    is FormalRegex.Concatenation -> when (left) {
        is FormalRegex.Alternative -> "(${left.toPrettyString()})"
        else -> left.toPrettyString()
    } + when (right) {
        is FormalRegex.Alternative -> "(${right.toPrettyString()})"
        else -> right.toPrettyString()
    }
    is FormalRegex.Alternative -> "${alternative1.toPrettyString()}|${alternative2.toPrettyString()}"
}

val FormalRegex?.alternatives: List<FormalRegex?> get() = when (this) {
    is FormalRegex.Alternative -> alternative1.alternatives + alternative2.alternatives
    else -> listOf(this)
}

val FormalRegex?.concats: List<FormalRegex> get() = when (this) {
    is FormalRegex.Concatenation -> listOfNotNull(left, right).flatMap { it.concats }
    else -> listOfNotNull(this)
}
