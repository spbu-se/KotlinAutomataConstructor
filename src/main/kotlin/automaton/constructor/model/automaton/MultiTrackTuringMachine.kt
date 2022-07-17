package automaton.constructor.model.automaton

import automaton.constructor.model.data.MultiTrackTuringMachineData
import automaton.constructor.model.memory.tape.MultiTrackTapeDescriptor

/**
 * Multi-track Turing machine.
 *
 * It's an automaton with several [tracks] as [memory descriptors][memoryDescriptors].
 */
class MultiTrackTuringMachine(
    val tracks: MultiTrackTapeDescriptor
) : AbstractAutomaton(NAME, memoryDescriptors = listOf(tracks)) {
    init {
        require(tracks.trackCount > 1) {
            "Illegal `tracks` argument when creating `MultiTrackTuringMachine`"
        }
    }

    override fun getTypeData() = MultiTrackTuringMachineData(
        tracks = tracks.getData()
    )

    companion object {
        const val NAME = "multi-track Turing machine"
    }
}
