package automaton.constructor.model.automaton

import automaton.constructor.model.memory.RegisterDescriptor
import automaton.constructor.model.memory.tape.MultiTrackTapeDescriptor

/**
 * Turing machine with registers.
 *
 * It's an automaton with a [tape] and several [registers] [memory descriptors][memoryDescriptors].
 */
class TuringMachineWithRegisters(
    val tape: MultiTrackTapeDescriptor = MultiTrackTapeDescriptor(trackCount = 1),
    val registers: List<RegisterDescriptor> = List(DEFAULT_REGISTER_COUNT) { RegisterDescriptor() },
) : Automaton by BaseAutomaton(NAME, memoryDescriptors = listOf(tape) + registers) {
    init {
        require(tape.trackCount == 1) {
            "Illegal `tape` argument when creating `TuringMachineWithRegisters`"
        }
        require(registers.size in MIN_REGISTER_COUNT..MAX_REGISTER_COUNT) {
            "Illegal `registers` argument when creating `TuringMachineWithRegisters`"
        }
    }

    companion object {
        const val NAME = "Turing machine with registers"
        const val MIN_REGISTER_COUNT = 1
        const val MAX_REGISTER_COUNT = 5
        const val DEFAULT_REGISTER_COUNT = 1
    }
}
