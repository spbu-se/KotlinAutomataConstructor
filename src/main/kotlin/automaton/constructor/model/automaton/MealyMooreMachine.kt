package automaton.constructor.model.automaton

import automaton.constructor.model.memory.output.MealyOutputDescriptor
import automaton.constructor.model.memory.output.MooreOutputDescriptor
import automaton.constructor.model.memory.tape.InputTapeDescriptor

/**
 * Mealy/Moore machine.
 *
 * It's an automaton with an [input tape][inputTape], a [Mealy output][mealyOutput], and a [Moore output][mooreOutput] as [memory descriptors][memoryDescriptors].
 */
class MealyMooreMachine(
    val inputTape: InputTapeDescriptor,
    val mealyOutput: MealyOutputDescriptor,
    val mooreOutput: MooreOutputDescriptor
) : Automaton by BaseAutomaton(NAME, memoryDescriptors = listOf(inputTape, mealyOutput, mooreOutput)) {
    companion object {
        const val NAME = "Mealy/Moore machine"
    }
}
