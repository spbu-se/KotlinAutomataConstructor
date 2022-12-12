package automaton.constructor.model.transformation

import automaton.constructor.model.TestAutomatons
import automaton.constructor.model.action.ActionFailedException
import automaton.constructor.model.action.perform
import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.automaton.FiniteAutomaton
import automaton.constructor.model.data.getData
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class AutomatonDeterminizationTest {
    @Test
    fun `should fail to determinize already deterministic`() {
        assertThrows<ActionFailedException> {
            TestAutomatons.SINGLE_A_RECOGNISER_WITH_EPSILON_LOOPS_DETERMINIZED.determinize()
        }
    }

    @Test
    fun `should fail to determinize without init states`() {
        assertThrows<ActionFailedException> {
            TestAutomatons.NO_INIT_STATE.determinize()
        }
    }

    @Test
    fun `should correctly determinize epsilon loops`() {
        assertEquals(
            TestAutomatons.SINGLE_A_RECOGNISER_WITH_EPSILON_LOOPS_DETERMINIZED.getData(),
            TestAutomatons.SINGLE_A_RECOGNISER_WITH_EPSILON_LOOPS.determinize().getData()
        )
    }

    @Test
    fun `should correctly determinize redundancy`() {
        assertEquals(
            TestAutomatons.SINGLE_A_RECOGNISER_WITH_REDUNDANCY_DETERMINIZED.getData(),
            TestAutomatons.SINGLE_A_RECOGNISER_WITH_REDUNDANCY.determinize().getData()
        )
    }

    @Test
    fun `should correctly determinize two coprime cycles with same symbol`() {
        assertEquals(
            TestAutomatons.COMPLICATED_ANY_UNARY_BUT_1_RECOGNISER_DETERMINIZED.getData(),
            TestAutomatons.COMPLICATED_ANY_UNARY_BUT_1_RECOGNISER.determinize().getData()
        )
    }

    private fun FiniteAutomaton.determinize(): Automaton {
        transformationActions.first { it is DeterminizeAutomatonAction }.perform()
        val transformation = isInputForTransformation
        transformation!!.complete()
        return transformation.resultingAutomaton
    }
}
