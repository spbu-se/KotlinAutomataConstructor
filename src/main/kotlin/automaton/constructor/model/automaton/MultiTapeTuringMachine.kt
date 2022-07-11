package automaton.constructor.model.automaton

import automaton.constructor.model.memory.tape.MultiTrackTapeDescriptor

/**
 * Multi-tape Turing machine.
 *
 * It's an automaton with several [tapes] [memory descriptors][memoryDescriptors].
 */
class MultiTapeTuringMachine(
    val tapes: List<MultiTrackTapeDescriptor> = List(DEFAULT_TAPE_COUNT) {
        MultiTrackTapeDescriptor(trackCount = 1)
    },
) : Automaton by BaseAutomaton(NAME, memoryDescriptors = tapes) {
    init {
        require(tapes.size in MIN_TAPE_COUNT..MAX_TAPE_COUNT && tapes.all { it.trackCount == 1 }) {
            "Illegal `tapes` argument when creating `MultiTapeTuringMachine`"
        }
    }

    companion object {
        const val NAME = "multi-tape Turing machine"
        const val MIN_TAPE_COUNT = 2
        const val MAX_TAPE_COUNT = 5
        const val DEFAULT_TAPE_COUNT = 2
    }
}
