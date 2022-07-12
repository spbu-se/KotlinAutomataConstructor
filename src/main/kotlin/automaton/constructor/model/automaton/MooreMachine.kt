package automaton.constructor.model.automaton

import automaton.constructor.model.memory.output.MooreOutputDescriptor
import automaton.constructor.model.memory.tape.InputTapeDescriptor

/**
 * Moore machine.
 *
 * It's an automaton with an [input tape][inputTape] and a [Moore output][mooreOutput] [memory descriptors][memoryDescriptors].
 */
class MooreMachine(
    val inputTape: InputTapeDescriptor,
    val mooreOutput: MooreOutputDescriptor
) : Automaton by BaseAutomaton(NAME, memoryDescriptors = listOf(inputTape, mooreOutput)) {
    companion object {
        const val NAME = "Moore machine"
    }
}
