package automaton.constructor.model.automaton

import automaton.constructor.model.memory.tape.MultiTrackTapeDescriptor

/**
 * Turing machine.
 *
 * It's an automaton with a [tape] as a [memory descriptor][memoryDescriptors].
 */
class TuringMachine(
    val tape: MultiTrackTapeDescriptor
) : Automaton by BaseAutomaton(NAME, memoryDescriptors = listOf(tape)) {
    init {
        require(tape.trackCount == 1) {
            "Illegal `tape` argument when creating `TuringMachine`"
        }
    }

    companion object {
        const val NAME = "Turing machine"
    }
}
