package automaton.constructor.model.module

import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.TestAutomatons
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.Test
import kotlin.test.assertEquals

class ProblemDetectorTest {

    val correctAutomatons
        get() = listOf(
            TestAutomatons.BINARY_INCREMENT,
            TestAutomatons.EMPTY_INPUT_DETECTOR_WITH_EPSILON_LOOP,
            TestAutomatons.EVEN_PALINDROMES
        )

    @ParameterizedTest
    @MethodSource("getCorrectAutomatons")
    fun `correct automatons should have no problems`(automaton: Automaton) =
        assertEquals(emptyList(), automaton.problems)

    @Test
    fun `no-final-state should have no final state problem`() = assertEquals(
        setOf(ProblemDetector.ADD_FINAL_STATE_MESSAGE),
        TestAutomatons.NO_FINAL_STATE.problems.map { it.message }.toSet()
    )

    @Test
    fun `no-init-state should have no init state problem`() = assertEquals(
        setOf(ProblemDetector.ADD_INIT_STATE_MESSAGE),
        TestAutomatons.NO_INIT_STATE.problems.map { it.message }.toSet()
    )

    @Test
    fun `no-states should have no init state and no final state problems`() = assertEquals(
        setOf(ProblemDetector.ADD_INIT_STATE_MESSAGE, ProblemDetector.ADD_FINAL_STATE_MESSAGE),
        TestAutomatons.NO_STATES.problems.map { it.message }.toSet()
    )

    @Test
    fun `transition-from-final-state should have transition from final state problem`() = assertEquals(
        setOf(ProblemDetector.REMOVE_TRANSITIONS_FROM_FINAL_STATES_MESSAGE),
        TestAutomatons.TRANSITION_FROM_FINAL_STATE.problems.map { it.message }.toSet()
    )
}
