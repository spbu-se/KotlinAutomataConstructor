package automaton.constructor.model.automaton

import automaton.constructor.model.action.element.createMealyToMooreElementAction
import automaton.constructor.model.action.element.createMooreToMealyElementAction
import automaton.constructor.model.automaton.flavours.AutomatonWithInputTape
import automaton.constructor.model.automaton.flavours.AutomatonWithMealyMooreOutputTape
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
    override val inputTape: InputTapeDescriptor,
    override val mealyMooreOutputTape: MealyMooreOutputTapeDescriptor
) : AbstractAutomaton(
    DISPLAY_NAME,
    memoryDescriptors = listOf(inputTape, mealyMooreOutputTape),
    I18N.messages.getString("MealyMooreMachine.Deterministic"),
    I18N.messages.getString("MealyMooreMachine.Nondeterministic"),
    I18N.messages.getString("OpenedAutomatonController.MealyMooreMachine.Untitled")
),
    AutomatonWithInputTape, AutomatonWithMealyMooreOutputTape {
    override fun getTypeData() = MealyMooreMachineData(
        inputTape = inputTape.getData(),
        mealyMooreOutputTape = mealyMooreOutputTape.getData()
    )

    override val stateActions = super.stateActions + listOf(
        createMooreToMealyElementAction(mealyMooreMachine = this),
        createMealyToMooreElementAction(mealyMooreMachine = this)
    )

    companion object {
        val DISPLAY_NAME: String = I18N.messages.getString("MealyMooreMachine")
    }
}
