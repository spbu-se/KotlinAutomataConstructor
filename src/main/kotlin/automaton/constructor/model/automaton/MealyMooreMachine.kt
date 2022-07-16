package automaton.constructor.model.automaton

import automaton.constructor.model.memory.output.MealyMooreOutputTapeDescriptor
import automaton.constructor.model.memory.tape.InputTapeDescriptor
import automaton.constructor.utils.I18N.labels

/**
 * Mealy/Moore machine.
 *
 * It's an automaton with an [input tape][inputTape] and a [Mealy/Moore output tape][mealyMooreOutputTape] as [memory descriptors][memoryDescriptors].
 */
class MealyMooreMachine(
    val inputTape: InputTapeDescriptor,
    val mealyMooreOutputTape: MealyMooreOutputTapeDescriptor
) : Automaton by BaseAutomaton(NAME, memoryDescriptors = listOf(inputTape, mealyMooreOutputTape)) {
    companion object {
        val NAME: String = labels.getString("MealyMooreMachine.NAME")
    }
}
