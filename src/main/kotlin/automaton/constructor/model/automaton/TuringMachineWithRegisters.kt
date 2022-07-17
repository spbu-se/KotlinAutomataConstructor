package automaton.constructor.model.automaton

import automaton.constructor.model.memory.RegisterDescriptor
import automaton.constructor.model.memory.tape.MultiTrackTapeDescriptor
import automaton.constructor.utils.I18N.messages

/**
 * Turing machine with registers.
 *
 * It's an automaton with a [tape] and several [registers] as [memory descriptors][memoryDescriptors].
 */
class TuringMachineWithRegisters(
    val tape: MultiTrackTapeDescriptor,
    val registers: List<RegisterDescriptor>
) : Automaton by BaseAutomaton(NAME, memoryDescriptors = listOf(tape) + registers) {
    init {
        require(tape.trackCount == 1) {
            messages.getString("TuringMachineWithRegisters.IllegalTapeArgument")
        }
        require(registers.isNotEmpty()) {
            messages.getString("TuringMachineWithRegisters.IllegalRegistersArgument")
        }
    }

    companion object {
        val NAME: String = messages.getString("TuringMachineWithRegisters")
    }
}
