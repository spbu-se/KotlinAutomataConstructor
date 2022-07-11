package automaton.constructor.model.automaton

import automaton.constructor.model.memory.tape.MultiTrackTapeDescriptor

/**
 * Turing machine.
 *
 * It's an automaton with a [tape] [memory descriptor][memoryDescriptors].
 */
class TuringMachine(
    val tape: MultiTrackTapeDescriptor = MultiTrackTapeDescriptor(trackCount = 1),
) : Automaton by BaseAutomaton(NAME, memoryDescriptors = listOf(tape)) {
    companion object {
        const val NAME = "Turing machine"
    }
}
