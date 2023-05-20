package automaton.constructor.model.transformation

import automaton.constructor.model.TestAutomatons
import automaton.constructor.model.action.ActionFailedException
import automaton.constructor.model.action.perform
import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.data.getData
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import tornadofx.onChangeOnce
import java.util.stream.Stream
import kotlin.test.assertEquals

class MinimizeTransformationTest {
    @ParameterizedTest
    @MethodSource("should minimize correctly source")
    fun `should minimize correctly`(description: String, input: Automaton, output: Automaton) {
        assertEquals(
            output.getData(),
            input.minimizeViaAction().getData()
        )
    }

    private fun `should minimize correctly source`(): Stream<Arguments> = Stream.of(
        Arguments.of("c alias", TestAutomatons.C_ALIAS_DFA, TestAutomatons.C_ALIAS_MIN_DFA),
        Arguments.of("dead state", TestAutomatons.DEAD_STATE, TestAutomatons.USELESS_STATE_REMOVED),
        Arguments.of("unreach state", TestAutomatons.UNREACH_STATE, TestAutomatons.USELESS_STATE_REMOVED),
    )

    @ParameterizedTest
    @MethodSource("should throw ActionFailedException for already deterministic source")
    fun `should throw ActionFailedException for already deterministic`(description: String, input: Automaton) {
        assertThrows<ActionFailedException> {
            input.minimizeViaAction()
        }
    }

    private fun `should throw ActionFailedException for already deterministic source`(): Stream<Arguments> = Stream.of(
        Arguments.of("c alias", TestAutomatons.C_ALIAS_MIN_DFA),
        Arguments.of("useless state removed", TestAutomatons.USELESS_STATE_REMOVED),
    )

    private fun Automaton.minimizeViaAction(): Automaton {
        lateinit var output: Automaton
        isInputForTransformationProperty.onChangeOnce { transformation ->
            transformation!!.complete()
            output = transformation.resultingAutomaton
        }
        transformationActions.first { it is MinimizeAction }.perform()
        return output
    }
}