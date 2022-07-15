package automaton.constructor.model.automaton

import automaton.constructor.model.memory.RegisterDescriptor
import automaton.constructor.model.memory.tape.InputTapeDescriptor
import automaton.constructor.utils.I18N.labels

/**
 * Register automaton.
 *
 * It's an automaton with an [input tape][inputTape] and several [registers] as [memory descriptors][memoryDescriptors].
 */
class RegisterAutomaton(
    val inputTape: InputTapeDescriptor,
    val registers: List<RegisterDescriptor>
) : Automaton by BaseAutomaton(NAME, memoryDescriptors = listOf(inputTape) + registers) {
    init {
        require(registers.isNotEmpty()) {
            labels.getString("RegisterAutomaton.IllegalRegistersArgument")
        }
    }

    companion object {
        val NAME: String = labels.getString("RegisterAutomaton.NAME")
    }
}
