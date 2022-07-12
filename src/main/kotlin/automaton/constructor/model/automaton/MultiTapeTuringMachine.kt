package automaton.constructor.model.automaton

import automaton.constructor.model.memory.tape.MultiTrackTapeDescriptor

/**
 * Multi-tape Turing machine.
 *
 * It's an automaton with several [tapes] as [memory descriptors][memoryDescriptors].
 */
class MultiTapeTuringMachine(
    val tapes: List<MultiTrackTapeDescriptor>
) : Automaton by BaseAutomaton(NAME, memoryDescriptors = tapes) {
    init {
        require(tapes.isNotEmpty() && tapes.all { it.trackCount == 1 }) {
            "Illegal `tapes` argument when creating `MultiTapeTuringMachine`"
        }
    }

    companion object {
        const val NAME = "multi-tape Turing machine"
    }
}
