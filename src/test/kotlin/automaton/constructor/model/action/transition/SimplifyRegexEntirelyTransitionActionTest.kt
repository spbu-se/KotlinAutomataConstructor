package automaton.constructor.model.action.transition

import automaton.constructor.model.TestAutomatons
import automaton.constructor.model.data.getData
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class SimplifyRegexEntirelyTransitionActionTest {
    @Test
    fun `C alias regex get correctly simplified`() {
        val automaton = TestAutomatons.C_ALIAS_REGEX
        automaton.transitionActions.first {
            it is SimplifyRegexEntirelyTransitionAction
        }.performOn(automaton.transitions.single())
        assertEquals(
            TestAutomatons.C_ALIAS_NFA.getData(),
            automaton.getData()
        )
    }
}