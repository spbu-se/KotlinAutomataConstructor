package automaton.constructor.model.transformation

import automaton.constructor.model.action.Action
import automaton.constructor.model.action.ActionAvailability
import automaton.constructor.model.action.ActionFailedException
import automaton.constructor.model.action.perform
import automaton.constructor.model.automaton.CustomAutomaton
import automaton.constructor.model.element.State
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import javafx.scene.input.KeyCombination
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class StatewiseTransformationTest {
    @Test
    fun `should perform state action on states available for actions`() {
        val automaton = CustomAutomaton(emptyList())
        automaton.addState("S1")
        automaton.addState("S2")
        automaton.addState("S3")

        val stateAction = mockk<Action<State>>(relaxed = true)

        every { stateAction.getAvailabilityFor(match { it.name == "S1" }) } returns ActionAvailability.HIDDEN
        every { stateAction.getAvailabilityFor(match { it.name == "S2" }) } returns ActionAvailability.DISABLED
        every { stateAction.getAvailabilityFor(match { it.name == "S3" }) } returns ActionAvailability.AVAILABLE

        every { stateAction.performOn(match { it.name != "S3" }) } throws Exception("Unexpected perform on unavailable state")

        val stateWiseAction = StatewiseTransformationAction(
            displayName = "Test statewise action",
            automaton = automaton,
            unavailableMessage = "Test unavailable message",
            keyCombination = KeyCombination.NO_MATCH,
            stateActionFinder = { stateAction }
        )

        stateWiseAction.perform()

        verify {
            stateAction.performOn(match { it.name == "S3" })
        }
    }

    @Test
    fun `should throw ActionFailedException if no states are available for action`() {
        val automaton = CustomAutomaton(emptyList())
        automaton.addState("S1")
        automaton.addState("S2")

        val stateAction = mockk<Action<State>>(relaxed = true)

        every { stateAction.getAvailabilityFor(match { it.name == "S1" }) } returns ActionAvailability.HIDDEN
        every { stateAction.getAvailabilityFor(match { it.name == "S2" }) } returns ActionAvailability.DISABLED

        every { stateAction.performOn(any()) } throws Exception("Unexpected perform on unavailable state")

        val stateWiseAction = StatewiseTransformationAction(
            displayName = "Test statewise action",
            automaton = automaton,
            unavailableMessage = "Test unavailable message",
            keyCombination = KeyCombination.NO_MATCH,
            stateActionFinder = { stateAction }
        )

        assertThrows<ActionFailedException> {
            stateWiseAction.perform()
        }
    }
}