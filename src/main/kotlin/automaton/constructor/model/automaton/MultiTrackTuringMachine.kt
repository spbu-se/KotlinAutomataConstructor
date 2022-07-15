package automaton.constructor.model.automaton

import automaton.constructor.model.memory.tape.MultiTrackTapeDescriptor
import automaton.constructor.utils.I18N.labels

/**
 * Multi-track Turing machine.
 *
 * It's an automaton with several [tracks] as [memory descriptors][memoryDescriptors].
 */
class MultiTrackTuringMachine(
    val tracks: MultiTrackTapeDescriptor
) : Automaton by BaseAutomaton(NAME, memoryDescriptors = listOf(tracks)) {
    init {
        require(tracks.trackCount > 1) {
            labels.getString("MultiTrackTuringMachine.IllegalTracksArgument")
        }
    }

    companion object {
        val NAME: String = labels.getString("MultiTrackTuringMachine.NAME")
    }
}
