package automaton.constructor.model.automaton

import automaton.constructor.model.action.state.MealyToMooreAction
import automaton.constructor.model.action.state.MooreToMealyAction
import automaton.constructor.model.memory.tape.InputTapeDescriptor
import automaton.constructor.model.memory.tape.OutputTapeDescriptor
import automaton.constructor.model.transformation.StatewiseTransformationAction
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class MealyMooreMachineTest : AbstractAutomatonTest() {
    override fun createAutomaton() = MealyMooreMachine(InputTapeDescriptor(), OutputTapeDescriptor())

    @Test
    fun `should have statewise transformation for MealyToMooreAction`() {
        val machine = createAutomaton()
        assertTrue(machine.transformationActions.any {
            it is StatewiseTransformationAction && it.stateActionFinder(machine) is MealyToMooreAction
        })
    }

    @Test
    fun `should have statewise transformation for MooreToMealyAction`() {
        val machine = createAutomaton()
        assertTrue(machine.transformationActions.any {
            it is StatewiseTransformationAction && it.stateActionFinder(machine) is MooreToMealyAction
        })
    }
}