package automaton.constructor.model.action.state

import automaton.constructor.model.TestAutomatons
import automaton.constructor.model.action.ActionFailedException
import automaton.constructor.model.automaton.MealyMooreMachine
import automaton.constructor.model.data.getData
import automaton.constructor.model.property.EPSILON_VALUE
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class MooreToMealyActionTest {
    @Test
    fun `mealy-remove-zeros-converted-to-moore should be correctly converted to mealy`() {
        val machine = TestAutomatons.MEALY_REMOVE_ZEROES_CONVERTED_TO_MOORE
        machine.convertToMealy("S0", "1")
        assertEquals(
            TestAutomatons.MEALY_REMOVE_ZEROES_CONVERTED_TO_MOORE_AND_BACK.getData(),
            machine.getData()
        )
    }

    @Test
    fun `moore-identity should be correctly converted to mealy`() {
        val machine = TestAutomatons.MOORE_IDENTITY
        machine.convertToMealy("0", "0")
        machine.convertToMealy("1", "1")
        assertEquals(
            TestAutomatons.MOORE_IDENTITY_CONVERTED_TO_MEALY.getData(),
            machine.getData()
        )
    }

    @Test
    fun `should fail to convert state without output`() {
        val machine = TestAutomatons.MOORE_IDENTITY
        assertThrows<ActionFailedException> {
            machine.convertToMealy("S0", EPSILON_VALUE)
        }
    }

    private fun MealyMooreMachine.convertToMealy(state: String, output: String?) {
        stateActions.first { it is MooreToMealyAction }.performOn(
            states.first { it.name == state && it.outputValue == output }
        )
    }
}
