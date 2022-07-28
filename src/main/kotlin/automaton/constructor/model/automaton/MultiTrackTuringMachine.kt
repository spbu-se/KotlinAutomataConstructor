package automaton.constructor.model.automaton

import automaton.constructor.model.automaton.flavours.AutomatonWithTracks
import automaton.constructor.model.data.MultiTrackTuringMachineData
import automaton.constructor.model.memory.tape.MultiTrackTapeDescriptor
import automaton.constructor.utils.I18N

/**
 * Multi-track Turing machine.
 *
 * It's an automaton with several [tracks] as [memory descriptors][memoryDescriptors].
 */
class MultiTrackTuringMachine(
    override val tracks: MultiTrackTapeDescriptor
) : AbstractAutomaton(
    DISPLAY_NAME,
    memoryDescriptors = listOf(tracks),
    I18N.messages.getString("MultiTrackTuringMachine.Deterministic"),
    I18N.messages.getString("MultiTrackTuringMachine.Nondeterministic"),
    I18N.messages.getString("OpenedAutomatonController.MultiTrackTuringMachine.Untitled")
), AutomatonWithTracks {
    init {
        require(tracks.trackCount > 1) {
            "Illegal `tracks` argument when creating `MultiTrackTuringMachine`"
        }
    }

    override fun getTypeData() = MultiTrackTuringMachineData(
        tracks = tracks.getData()
    )

    companion object {
        val DISPLAY_NAME: String = I18N.messages.getString("MultiTrackTuringMachine")
    }
}
