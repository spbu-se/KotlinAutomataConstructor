package automaton.constructor.model.automaton

import automaton.constructor.model.data.MultiTrackTuringMachineData
import automaton.constructor.model.memory.tape.MultiTrackTapeDescriptor
import automaton.constructor.utils.I18N.messages

/**
 * Multi-track Turing machine.
 *
 * It's an automaton with several [tracks] as [memory descriptors][memoryDescriptors].
 */
class MultiTrackTuringMachine(
    val tracks: MultiTrackTapeDescriptor
) : AbstractAutomaton(DISPLAY_NAME, memoryDescriptors = listOf(tracks)) {
    init {
        require(tracks.trackCount > 1) {
            messages.getString("MultiTrackTuringMachine.IllegalTracksArgument")
        }
    }

    override fun getTypeData() = MultiTrackTuringMachineData(
        tracks = tracks.getData()
    )

    companion object {
        val DISPLAY_NAME: String = messages.getString("MultiTrackTuringMachine")
    }
}
