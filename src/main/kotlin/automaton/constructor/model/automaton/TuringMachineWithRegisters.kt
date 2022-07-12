package automaton.constructor.model.automaton

import automaton.constructor.model.memory.RegisterDescriptor
import automaton.constructor.model.memory.tape.MultiTrackTapeDescriptor

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
            "Illegal `tape` argument when creating `TuringMachineWithRegisters`"
        }
        require(registers.isNotEmpty()) {
            "Illegal `registers` argument when creating `TuringMachineWithRegisters`"
        }
    }

    companion object {
        const val NAME = "Turing machine with registers"
    }
}
