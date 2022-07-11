package automaton.constructor.model.automaton

import automaton.constructor.model.memory.tape.MultiTrackTapeDescriptor

/**
 * Multi-track Turing machine.
 *
 * It's an automaton with several [tracks] [memory descriptors][memoryDescriptors].
 */
class MultiTrackTuringMachine(
    val tracks: MultiTrackTapeDescriptor = MultiTrackTapeDescriptor(DEFAULT_TRACK_COUNT),
) : Automaton by BaseAutomaton(NAME, memoryDescriptors = listOf(tracks)) {
    init {
        require(tracks.trackCount in MIN_TRACK_COUNT..MAX_TRACK_COUNT) {
            "Illegal `tracks` argument when creating `MultiTrackTuringMachine`"
        }
    }

    companion object {
        const val NAME = "multi-track Turing machine"
        const val MIN_TRACK_COUNT = 2
        const val MAX_TRACK_COUNT = 5
        const val DEFAULT_TRACK_COUNT = 2
    }
}
