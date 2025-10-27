package automaton.constructor.model.grammar

import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser

/**
 * Recursive Automaton Regex (RARegex) supporting terminals (single-quoted chars), terminal strings (double-quoted strings) and Nonterminal references.
 */
sealed class RARegex {
    data class Terminal(val ch: Char) : RARegex()
    data class NonTerminalRef(val name: String) : RARegex()
    data class Concat(val left: RARegex?, val right: RARegex?) : RARegex()
    data class Alt(val a: RARegex?, val b: RARegex?) : RARegex()
    data class KleeneStar(val inner: RARegex?) : RARegex()
    object Eps : RARegex()

    companion object {
        private val grammar = object : Grammar<RARegex?>() {
            val nonterminal by regexToken("""[A-Za-z][A-Za-z0-9_]*""")
            val eps by literalToken("$")
            val quotedTerminal by regexToken("'(?:[^'\\\\]|\\\\.)*'")
            val doubleQuotedTerminalString by regexToken("\"(?:[^\"\\\\]|\\\\.)*\"")
            val lpar by literalToken("(")
            val rpar by literalToken(")")
            val bar by literalToken("|")
            val star by literalToken("*")
            val ws by regexToken("""\s+""", ignore = true)

            override val rootParser by parser(this::alt)


            private fun unescapeStringLiteral(inner: String): String {
                val sb = StringBuilder(inner.length)
                var i = 0
                while (i < inner.length) {
                    val ch = inner[i]
                    if (ch == '\\' && i + 1 < inner.length) {
                        when (val next = inner[i + 1]) {
                            'n' -> sb.append('\n')
                            't' -> sb.append('\t')
                            'r' -> sb.append('\r')
                            '\\' -> sb.append('\\')
                            '"' -> sb.append('"')
                            '\'' -> sb.append('\'')
                            'u' -> {
                                if (i + 5 < inner.length) {
                                    val hex = inner.substring(i + 2, i + 6)
                                    try {
                                        sb.append(hex.toInt(16).toChar())
                                        i += 4
                                    } catch (_: NumberFormatException) {
                                        sb.append('u')
                                    }
                                } else sb.append('u')
                            }

                            else -> sb.append(next)
                        }
                        i += 2
                    } else {
                        sb.append(ch)
                        i++
                    }
                }
                return sb.toString()
            }

            private fun stringToRegex(s: String): RARegex {
                if (s.isEmpty()) return Eps
                val list = s.map { Terminal(it) as RARegex }
                return list.reduceOrNull { acc, r -> Concat(acc, r) } ?: list.first()
            }

            val refOrTerm: Parser<RARegex?> by ((nonterminal use { NonTerminalRef(text) }) or (eps asJust Eps) or (doubleQuotedTerminalString use {
                val inner = text.substring(1, text.length - 1)
                stringToRegex(unescapeStringLiteral(inner))
            }) or (quotedTerminal use {
                val inner = text.substring(1, text.length - 1)
                stringToRegex(unescapeStringLiteral(inner))
            }) or (-lpar * parser(this::alt) * -rpar))
            val starred by (refOrTerm * -oneOrMore(star)).map { KleeneStar(it) } or refOrTerm
            val concat: Parser<RARegex?> by zeroOrMore(starred).map { list ->
                list.reduceOrNull { acc, r -> Concat(acc, r) } ?: Eps
            }

            fun alt() = leftAssociative(concat, bar) { l, _, r -> Alt(l, r) }
        }

        fun parse(s: String): RARegex? = try {
            grammar.parseToEnd(s)
        } catch (e: Exception) {
            throw e
        }
    }
}
