package automaton.constructor.model.action.transition

import automaton.constructor.model.TestAutomatons
import automaton.constructor.model.action.ActionFailedException
import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.data.getData
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals

class EliminateEpsilonTransitionActionTest {
    @Test
    fun `s1 s2 epsilon transition should be correctly eliminated in single-a-recogniser-with-epsilon-loops`() {
        val automaton = TestAutomatons.SINGLE_A_RECOGNISER_WITH_EPSILON_LOOPS
        automaton.eliminateEpsilonTransition("S1", "S2")
        assertEquals(
            TestAutomatons.SINGLE_A_RECOGNISER_WITH_EPSILON_LOOPS_AFTER_S1_S2_EPSILON_TRANSITION_ELIMINATED.getData(),
            automaton.getData()
        )
    }

    @Test
    fun `s1 s2 and s0 s1 epsilon transitions should be correctly eliminated in single-a-recogniser-with-epsilon-loops`() {
        val automaton = TestAutomatons.SINGLE_A_RECOGNISER_WITH_EPSILON_LOOPS
        automaton.eliminateEpsilonTransition("S1", "S2")
        automaton.eliminateEpsilonTransition("S0", "S1")
        assertEquals(
            TestAutomatons.SINGLE_A_RECOGNISER_WITH_EPSILON_LOOPS_AFTER_S1_S2_AND_S0_S1_EPSILON_TRANSITIONS_ELIMINATED.getData(),
            automaton.getData()
        )
    }

    @Test
    fun `s3 s4 epsilon transition should be correctly eliminated in single-a-recogniser-with-epsilon-loops`() {
        val automaton = TestAutomatons.SINGLE_A_RECOGNISER_WITH_EPSILON_LOOPS
        automaton.eliminateEpsilonTransition("S3", "S4")
        assertEquals(
            TestAutomatons.SINGLE_A_RECOGNISER_WITH_EPSILON_LOOPS_AFTER_S3_S4_EPSILON_TRANSITION_ELIMINATED.getData(),
            automaton.getData()
        )
    }

    @Test
    fun `epsilon loop should be correctly eliminated`() {
        val automaton = TestAutomatons.EMPTY_INPUT_DETECTOR_WITH_EPSILON_LOOP
        automaton.eliminateEpsilonTransition("S0", "S0")
        assertEquals(
            TestAutomatons.EMPTY_INPUT_DETECTOR_WITH_EPSILON_LOOP_WITH_EPSILON_ELIMINATED.getData(),
            automaton.getData()
        )
    }

    @Test
    fun `all epsilon transitions should be correctly eliminated in single-a-recogniser-with-epsilon-loops`() {
        val automaton = TestAutomatons.SINGLE_A_RECOGNISER_WITH_EPSILON_LOOPS
        automaton.eliminateEpsilonTransition("S0", "S1")
        automaton.eliminateEpsilonTransition("S0", "S2")
        automaton.eliminateEpsilonTransition("S4", "S5")
        automaton.eliminateEpsilonTransition("S4", "S3")
        automaton.eliminateEpsilonTransition("S3", "S4")
        assertEquals(
            TestAutomatons.SINGLE_A_RECOGNISER_WITH_EPSILON_LOOPS_AFTER_EPSILON_ELIMINATED.getData(),
            automaton.getData()
        )
    }

    @Test
    fun `epsilon transition should be correctly eliminated in complicated-empty-input-detector`() {
        val automaton = TestAutomatons.COMPLICATED_EMPTY_INPUT_DETECTOR
        automaton.eliminateEpsilonTransition("S0", "S1")
        assertEquals(
            TestAutomatons.COMPLICATED_EMPTY_INPUT_DETECTOR_WITH_EPSILON_ELIMINATED.getData(),
            automaton.getData()
        )
    }

    @Test
    fun `non epsilon transition should fail to get eliminated`() {
        val automaton = TestAutomatons.SINGLE_A_RECOGNISER_WITH_EPSILON_LOOPS
        assertThrows<ActionFailedException> {
            automaton.eliminateEpsilonTransition("S2", "S3")
        }
    }

    private fun Automaton.eliminateEpsilonTransition(source: String, target: String) {
        transitionActions.first { it is EliminateEpsilonTransitionAction }.performOn(
            transitions.first { it.source.name == source && it.target.name == target }
        )
    }
}
