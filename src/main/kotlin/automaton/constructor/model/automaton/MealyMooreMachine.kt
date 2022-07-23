package automaton.constructor.model.automaton

import automaton.constructor.model.action.EliminateEpsilonTransitionAction
import automaton.constructor.model.action.MealyToMooreAction
import automaton.constructor.model.action.MooreToMealyAction
import automaton.constructor.model.data.MealyMooreMachineData
import automaton.constructor.model.memory.output.MealyMooreOutputTapeDescriptor
import automaton.constructor.model.memory.tape.InputTapeDescriptor
import automaton.constructor.utils.I18N

/**
 * Mealy/Moore machine.
 *
 * It's an automaton with an [input tape][inputTape] and a [Mealy/Moore output tape][mealyMooreOutputTape] as [memory descriptors][memoryDescriptors].
 */
class MealyMooreMachine(
    val inputTape: InputTapeDescriptor,
    val mealyMooreOutputTape: MealyMooreOutputTapeDescriptor
) : AbstractAutomaton(DISPLAY_NAME, memoryDescriptors = listOf(inputTape, mealyMooreOutputTape)) {
    override fun getTypeData() = MealyMooreMachineData(
        inputTape = inputTape.getData(),
        mealyMooreOutputTape = mealyMooreOutputTape.getData()
    )

    override val transitionActions = super.transitionActions + listOf(
        EliminateEpsilonTransitionAction(automaton = this)
    )

    override val stateActions = super.stateActions + listOf(
        MooreToMealyAction(automaton = this),
        MealyToMooreAction(automaton = this)
    )

    companion object {
        val DISPLAY_NAME: String = I18N.messages.getString("MealyMooreMachine")
    }
}
