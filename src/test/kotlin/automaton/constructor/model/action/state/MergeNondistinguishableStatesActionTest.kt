package automaton.constructor.model.action.state

import automaton.constructor.model.TestAutomatons
import automaton.constructor.model.action.ActionFailedException
import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.data.getData
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.test.assertEquals

class MergeNondistinguishableStatesActionTest {
    @ParameterizedTest
    @MethodSource("should correctly merge nondistinguishable states source")
    fun `should correctly merge nondistinguishable states`(stateName: String, input: Automaton, output: Automaton) {
        input.stateActions.single { it is MergeNondistinguishableStatesAction }
            .performOn(input.states.single { it.name == stateName })
        assertEquals(
            output.getData(),
            input.getData()
        )
    }

    fun `should correctly merge nondistinguishable states source`(): Stream<Arguments> = Stream.of(
        Arguments.of("S0", TestAutomatons.NONDISTINGUISHABLE_STATES, TestAutomatons.NONDISTINGUISHABLE_STATES_MERGE_S0),
        Arguments.of("S1", TestAutomatons.NONDISTINGUISHABLE_STATES, TestAutomatons.NONDISTINGUISHABLE_STATES_MERGE_S1),
    )

    @Test
    fun `should throw ActionFailedException when state is distinguishable`() {
        val automaton = TestAutomatons.NONDISTINGUISHABLE_STATES_MERGE_S0
        val action = automaton.stateActions.single { it is MergeNondistinguishableStatesAction }
        val state = automaton.states.single { it.name == "S0" }
        assertThrows<ActionFailedException> {
            action.performOn(state)
        }
    }
}