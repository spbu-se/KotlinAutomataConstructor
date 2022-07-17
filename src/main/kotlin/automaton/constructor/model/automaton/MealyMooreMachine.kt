package automaton.constructor.model.automaton

import automaton.constructor.model.data.MealyMooreMachineData
import automaton.constructor.model.memory.output.MealyMooreOutputTapeDescriptor
import automaton.constructor.model.memory.tape.InputTapeDescriptor

/**
 * Mealy/Moore machine.
 *
 * It's an automaton with an [input tape][inputTape] and a [Mealy/Moore output tape][mealyMooreOutputTape] as [memory descriptors][memoryDescriptors].
 */
class MealyMooreMachine(
    val inputTape: InputTapeDescriptor,
    val mealyMooreOutputTape: MealyMooreOutputTapeDescriptor
) : Automaton by BaseAutomaton(NAME, memoryDescriptors = listOf(inputTape, mealyMooreOutputTape)) {
    override fun getTypeDataOrNull() = MealyMooreMachineData(
        inputTape = inputTape.getData(),
        mealyMooreOutputTape = mealyMooreOutputTape.getData()
    )

    companion object {
        const val NAME = "Mealy/Moore machine"
    }
}
