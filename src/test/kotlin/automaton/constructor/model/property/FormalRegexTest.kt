package automaton.constructor.model.property

import automaton.constructor.model.property.DynamicPropertyDescriptors.EPSILON_CHAR
import automaton.constructor.model.property.FormalRegex.Alternative
import automaton.constructor.model.property.FormalRegex.Concatenation
import automaton.constructor.model.property.FormalRegex.KleeneStar
import automaton.constructor.model.property.FormalRegex.Singleton
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.test.Test
import kotlin.test.assertEquals

class FormalRegexTest {

    private val stringsToRegexes = listOf(
        "" to EPSILON_VALUE,
        "$" to EPSILON_VALUE,
        EPSILON_CHAR.toString() to EPSILON_VALUE,
        "a" to Singleton('a'),
        "\\\\" to Singleton('\\'),
        "\\(" to Singleton('('),
        "\\)" to Singleton(')'),
        "\\|" to Singleton('|'),
        "\\*" to Singleton('*'),
        "ab" to Concatenation(Singleton('a'), Singleton('b')),
        "abc" to Concatenation(Concatenation(Singleton('a'), Singleton('b')), Singleton('c')),
        "a|b" to Alternative(Singleton('a'), Singleton('b')),
        "a|b|$" to Alternative(Alternative(Singleton('a'), Singleton('b')), null),
        "$|$|$" to Alternative(Alternative(null, null), null),
        "a*" to KleeneStar(Singleton('a')),
        "$*" to KleeneStar(EPSILON_VALUE),
        "(ab)*" to KleeneStar(Concatenation(Singleton('a'), Singleton('b'))),
        "ab*" to Concatenation(Singleton('a'), KleeneStar(Singleton('b'))),
        "ab|c" to Alternative(Concatenation(Singleton('a'), Singleton('b')), Singleton('c')),
        "a(b|c)" to Concatenation(Singleton('a'), Alternative(Singleton('b'), Singleton('c'))),
        "(b|c)d" to Concatenation(Alternative(Singleton('b'), Singleton('c')), Singleton('d')),
        "a*|c" to Alternative(KleeneStar(Singleton('a')), Singleton('c')),
        "a|c*" to Alternative(Singleton('a'), KleeneStar(Singleton('c'))),
        "(a|c)*" to KleeneStar(Alternative(Singleton('a'), Singleton('c'))),
        "đ((S|$)ā)*(S|$)(a(S|$))*d" to Concatenation(
            Concatenation(
                Concatenation(
                    Concatenation(
                        Singleton('đ'),
                        KleeneStar(Concatenation(Alternative(Singleton('S'), EPSILON_VALUE), Singleton('ā')))
                    ), Alternative(Singleton('S'), EPSILON_VALUE)
                ),
                KleeneStar(Concatenation(Singleton('a'), Alternative(Singleton('S'), EPSILON_VALUE)))
            ), Singleton('d')
        )
    )

    private fun stringsToRegexesSource(): Stream<Arguments> = stringsToRegexes.stream().map { (string, regex) ->
        Arguments.of(string, regex)
    }

    @ParameterizedTest
    @MethodSource("stringsToRegexesSource")
    fun testFromString(
        string: String,
        regex: FormalRegex?
    ) {
        assertEquals(
            regex,
            FormalRegex.fromString(string)
        )
    }

    @ParameterizedTest
    @MethodSource("stringsToRegexesSource")
    fun testToPrettyString(
        string: String,
        regex: FormalRegex?
    ) {
        assertEquals(
            string.replace('$', EPSILON_CHAR).ifEmpty { EPSILON_CHAR.toString() },
            regex?.toPrettyString() ?: EPSILON_CHAR.toString()
        )
    }

    @Test
    fun `alternatives should be epsilon for epsilon`() {
        assertEquals(
            listOf(EPSILON_VALUE),
            EPSILON_VALUE.alternatives
        )
    }

    @Test
    fun `concats should be empty for epsilon`() {
        assertEquals(
            emptyList(),
            EPSILON_VALUE.concats
        )
    }

    @Test
    fun `alternatives should be singleton for singleton`() {
        assertEquals(
            listOf(Singleton('a')),
            Singleton('a').alternatives
        )
    }

    @Test
    fun `concats should be singleton for singleton`() {
        assertEquals(
            listOf(Singleton('a')),
            Singleton('a').concats
        )
    }

    @Test
    fun `alternatives should be a multi element list for alternative`() {
        assertEquals(
            listOf(Singleton('a'), Singleton('b'), Singleton('c'), Singleton('d')),
            Alternative(
                Alternative(Singleton('a'), Singleton('b')),
                Alternative(Singleton('c'), Singleton('d'))
            ).alternatives
        )
    }

    @Test
    fun `concats should be a multi element list for concatenation`() {
        assertEquals(
            listOf(Singleton('a'), Singleton('b'), Singleton('c'), Singleton('d')),
            Concatenation(
                Concatenation(Singleton('a'), Singleton('b')),
                Concatenation(Singleton('c'), Singleton('d'))
            ).concats
        )
    }

    @Test
    fun `alternatives should not omit epsilon`() {
        assertEquals(
            listOf(EPSILON_VALUE, Singleton('b'), Singleton('c'), EPSILON_VALUE),
            Alternative(
                Alternative(EPSILON_VALUE, Singleton('b')),
                Alternative(Singleton('c'), EPSILON_VALUE)
            ).alternatives
        )
    }

    @Test
    fun `concats should omit epsilon`() {
        assertEquals(
            listOf(Singleton('b'), Singleton('c')),
            Concatenation(
                Concatenation(EPSILON_VALUE, Singleton('b')),
                Concatenation(Singleton('c'), EPSILON_VALUE)
            ).concats
        )
    }
}