package automaton.constructor.model.automaton

import automaton.constructor.model.memory.output.MealyOutputDescriptor
import automaton.constructor.model.memory.tape.InputTapeDescriptor
import automaton.constructor.utils.I18N.labels

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
        val NAME: String = labels.getString("MealyMachine.NAME")
    }
}
