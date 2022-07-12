package automaton.constructor.model.automaton

import automaton.constructor.model.memory.output.MealyOutputDescriptor
import automaton.constructor.model.memory.tape.InputTapeDescriptor

/**
 * Mealy machine.
 *
 * It's an automaton with an [input tape][inputTape] and a [Mealy output][mealyOutput] as [memory descriptors][memoryDescriptors].
 */
class MealyMachine(
    val inputTape: InputTapeDescriptor,
    val mealyOutput: MealyOutputDescriptor
) : Automaton by BaseAutomaton(NAME, memoryDescriptors = listOf(inputTape, mealyOutput)) {
    companion object {
        const val NAME = "Mealy machine"
    }
}
