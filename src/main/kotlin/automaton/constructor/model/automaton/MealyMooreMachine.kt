package automaton.constructor.model.automaton

import automaton.constructor.model.memory.output.MealyMooreOutputDescriptor
import automaton.constructor.model.memory.tape.InputTapeDescriptor

/**
 * Mealy/Moore machine.
 *
 * It's an automaton with an [input tape][inputTape] and a [Mealy/Moore output][mealyMooreOutput] as [memory descriptors][memoryDescriptors].
 */
class MealyMooreMachine(
    val inputTape: InputTapeDescriptor,
    val mealyMooreOutput: MealyMooreOutputDescriptor
) : Automaton by BaseAutomaton(NAME, memoryDescriptors = listOf(inputTape, mealyMooreOutput)) {
    companion object {
        const val NAME = "Mealy/Moore machine"
    }
}
