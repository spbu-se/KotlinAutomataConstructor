package automaton.constructor.model.automaton

import automaton.constructor.model.action.element.createMealyToMooreElementAction
import automaton.constructor.model.action.element.createMooreToMealyElementAction
import automaton.constructor.model.automaton.flavours.AutomatonWithInputTape
import automaton.constructor.model.automaton.flavours.AutomatonWithOutputTape
import automaton.constructor.model.data.MealyMooreMachineData
import automaton.constructor.model.memory.tape.InputTapeDescriptor
import automaton.constructor.model.memory.tape.OutputTapeDescriptor
import automaton.constructor.utils.I18N

/**
 * Mealy/Moore machine.
 *
 * It's an automaton with an [input tape][inputTape] and a [Mealy/Moore output tape][outputTape] as [memory descriptors][memoryDescriptors].
 */
class MealyMooreMachine(
    override val inputTape: InputTapeDescriptor,
    override val outputTape: OutputTapeDescriptor
) : AbstractAutomaton(
    DISPLAY_NAME,
    memoryDescriptors = listOf(inputTape, outputTape),
    I18N.messages.getString("MealyMooreMachine.Deterministic"),
    I18N.messages.getString("MealyMooreMachine.Nondeterministic"),
    I18N.messages.getString("MealyMooreMachine.Untitled")
),
    AutomatonWithInputTape, AutomatonWithOutputTape {
    override fun getTypeData() = MealyMooreMachineData(
        inputTape = inputTape.getData(),
        outputTape = outputTape.getData()
    )

    override fun createSubAutomaton() = MealyMooreMachine(inputTape, outputTape)

    override val stateActions = super.stateActions + listOf(
        createMooreToMealyElementAction(mealyMooreMachine = this),
        createMealyToMooreElementAction(mealyMooreMachine = this)
    )

    companion object {
        val DISPLAY_NAME: String = I18N.messages.getString("MealyMooreMachine")
    }
}
