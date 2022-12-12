package automaton.constructor.model.action.state

import automaton.constructor.model.TestAutomatons
import automaton.constructor.model.action.ActionFailedException
import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.data.getData
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class MealyToMooreActionTest {
    @Test
    fun `mealy-remove-zeros should be correctly converted to moore`() {
        val machine = TestAutomatons.MEALY_REMOVE_ZEROES
        machine.convertToMoore("S0")
        assertEquals(
            TestAutomatons.MEALY_REMOVE_ZEROES_CONVERTED_TO_MOORE.getData(),
            machine.getData()
        )
    }

    @Test
    fun `moore-identity-converted-to-mealy should be correctly converted to moore`() {
        val machine = TestAutomatons.MOORE_IDENTITY_CONVERTED_TO_MEALY
        machine.convertToMoore("0")
        machine.convertToMoore("1")
        assertEquals(
            TestAutomatons.MOORE_IDENTITY_CONVERTED_TO_MEALY_AND_BACK.getData(),
            machine.getData()
        )
    }

    @Test
    fun `should fail to convert state without incoming transitions`() {
        val machine = TestAutomatons.MOORE_IDENTITY_CONVERTED_TO_MEALY
        assertThrows<ActionFailedException> {
            machine.convertToMoore("S0")
        }
    }

    @Test
    fun `should fail to convert state without incoming transitions with output`() {
        val machine = TestAutomatons.MOORE_IDENTITY
        assertThrows<ActionFailedException> {
            machine.convertToMoore("0")
        }
    }

    private fun Automaton.convertToMoore(state: String) {
        stateActions.first { it is MealyToMooreAction }.performOn(
            states.first { it.name == state }
        )
    }
}
