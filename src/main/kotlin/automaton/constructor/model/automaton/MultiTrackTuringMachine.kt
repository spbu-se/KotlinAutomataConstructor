package automaton.constructor.model.automaton

import automaton.constructor.model.automaton.flavours.AutomatonWithTracks
import automaton.constructor.model.data.MultiTrackTuringMachineData
import automaton.constructor.model.memory.tape.MultiTrackTapeDescriptor
import automaton.constructor.utils.I18N
import automaton.constructor.utils.I18N.messages

/**
 * Multi-track Turing machine.
 *
 * It's an automaton with several [tracks] as [memory descriptors][memoryDescriptors].
 */
class MultiTrackTuringMachine(
    override val tracks: MultiTrackTapeDescriptor
) : AbstractAutomaton(DISPLAY_NAME, memoryDescriptors = listOf(tracks)), AutomatonWithTracks {
    init {
        require(tracks.trackCount > 1) {
            "Illegal `tracks` argument when creating `MultiTrackTuringMachine`"
        }
    }

    override val deterministicDisplayName: String =
        I18N.messages.getString("AutomatonDescriptionProvider.TuringMachine.Deterministic")
    override val nondeterministicDisplayName: String =
        I18N.messages.getString("AutomatonDescriptionProvider.TuringMachine.Nondeterministic")
    override val untitledDisplayName: String =
        I18N.messages.getString("OpenedAutomatonController.UntitledTuringMachine")

    override fun getTypeData() = MultiTrackTuringMachineData(
        tracks = tracks.getData()
    )

    companion object {
        val DISPLAY_NAME: String = messages.getString("MultiTrackTuringMachine")
    }
}
