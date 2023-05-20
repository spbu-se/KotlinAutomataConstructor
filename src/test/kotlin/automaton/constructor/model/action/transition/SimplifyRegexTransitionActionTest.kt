package automaton.constructor.model.action.transition

import automaton.constructor.model.TestAutomatons
import automaton.constructor.model.action.ActionFailedException
import automaton.constructor.model.automaton.FiniteAutomaton
import automaton.constructor.model.data.getData
import automaton.constructor.model.element.Transition
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.lang.reflect.InvocationTargetException
import java.util.stream.Stream
import kotlin.test.assertEquals
import kotlin.test.assertIs

class SimplifyRegexTransitionActionTest {
    @ParameterizedTest
    @MethodSource("validSource")
    fun testOnValid(description: String, input: FiniteAutomaton, output: FiniteAutomaton) {
        input.transitionActions.first {
            it is SimplifyRegexTransitionAction
        }.performOn(input.transitions.single())
        assertEquals(output.getData(), input.getData())
    }

    private fun validSource(): Stream<Arguments> = Stream.of(
        Arguments.of("kleene star", TestAutomatons.KLEENE_STAR, TestAutomatons.KLENEE_STAR_SIMPLIFIED),
        Arguments.of("kleene star loop", TestAutomatons.KLEENE_STAR_LOOP, TestAutomatons.KLEENE_STAR_LOOP_SIMPLIFIED),
        Arguments.of("concat", TestAutomatons.CONCAT, TestAutomatons.CONCAT_SIMPLIFIED),
        Arguments.of("concat epsilon", TestAutomatons.CONCAT_EPSILON, TestAutomatons.CONCAT_EPSILON_SIMPLIFIED),
        Arguments.of(
            "concat double epsilon",
            TestAutomatons.CONCAT_DOUBLE_EPSILON,
            TestAutomatons.CONCAT_DOUBLE_EPSILON_SIMPLIFIED
        ),
        Arguments.of("alternative", TestAutomatons.ALTERNATIVE, TestAutomatons.ALTERNATIVE_SIMPLIFIED),
    )

    @ParameterizedTest
    @MethodSource("invalidSource")
    fun testOnInvalid(description: String, input: FiniteAutomaton) {
        assertThrows<ActionFailedException> {
            input.transitionActions.first {
                it is SimplifyRegexTransitionAction
            }.performOn(input.transitions.single())
        }
    }

    @ParameterizedTest
    @MethodSource("invalidSource")
    fun testDoPerformOnInvalid(description: String, input: FiniteAutomaton) {
        assertIs<ActionFailedException>(
            assertThrows<InvocationTargetException> {
                SimplifyRegexTransitionAction::class.java
                    .getDeclaredMethod("doPerformOn", FiniteAutomaton::class.java, Transition::class.java)
                    .also { it.isAccessible = true }
                    .invoke(
                        input.transitionActions.first {
                            it is SimplifyRegexTransitionAction
                        },
                        input,
                        input.transitions.single()
                    )
            }.cause
        )
    }

    private fun invalidSource(): Stream<Arguments> = Stream.of(
        Arguments.of("single a", TestAutomatons.SINGLE_A),
        Arguments.of("epsilon", TestAutomatons.EPSILON),
    )
}