package automaton.constructor.model.automaton

import automaton.constructor.model.memory.output.MooreOutputDescriptor
import automaton.constructor.model.memory.tape.InputTapeDescriptor
import automaton.constructor.utils.I18N.labels

/**
 * Moore machine.
 *
 * It's an automaton with an [input tape][inputTape] and a [Moore output][mooreOutput] as [memory descriptors][memoryDescriptors].
 */
class MooreMachine(
    val inputTape: InputTapeDescriptor,
    val mooreOutput: MooreOutputDescriptor
) : Automaton by BaseAutomaton(NAME, memoryDescriptors = listOf(inputTape, mooreOutput)) {
    companion object {
        val NAME: String = labels.getString("MooreMachine.NAME")
    }
}
