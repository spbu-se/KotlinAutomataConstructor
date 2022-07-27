package automaton.constructor.model.automaton

import automaton.constructor.model.automaton.flavours.AutomatonWithTapes
import automaton.constructor.model.data.MultiTapeTuringMachineData
import automaton.constructor.model.memory.tape.MultiTrackTapeDescriptor
import automaton.constructor.utils.I18N
import automaton.constructor.utils.I18N.messages

/**
 * Multi-tape Turing machine.
 *
 * It's an automaton with several [tapes] as [memory descriptors][memoryDescriptors].
 */
class MultiTapeTuringMachine(
    override val tapes: List<MultiTrackTapeDescriptor>
) : AbstractAutomaton(DISPLAY_NAME, memoryDescriptors = tapes), AutomatonWithTapes {
    init {
        require(tapes.isNotEmpty() && tapes.all { it.trackCount == 1 }) {
            "Illegal `tapes` argument when creating `MultiTapeTuringMachine`"
        }
    }

    override val deterministicDisplayName: String =
        I18N.messages.getString("AutomatonDescriptionProvider.TuringMachine.Deterministic")
    override val nondeterministicDisplayName: String =
        I18N.messages.getString("AutomatonDescriptionProvider.TuringMachine.Nondeterministic")
    override val untitledDisplayName: String =
        I18N.messages.getString("OpenedAutomatonController.UntitledTuringMachine")

    override fun getTypeData() = MultiTapeTuringMachineData(tapes = tapes.map { it.getData() })

    companion object {
        val DISPLAY_NAME: String = messages.getString("MultiTapeTuringMachine")
    }
}
