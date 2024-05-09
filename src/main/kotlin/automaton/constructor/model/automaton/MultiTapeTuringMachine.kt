package automaton.constructor.model.automaton

import automaton.constructor.model.automaton.flavours.AutomatonWithTapes
import automaton.constructor.model.data.MultiTapeTuringMachineData
import automaton.constructor.model.memory.MemoryUnitDescriptor
import automaton.constructor.model.memory.tape.MultiTrackTapeDescriptor
import automaton.constructor.utils.I18N

/**
 * Multi-tape Turing machine.
 *
 * It's an automaton with several [tapes] as [memory descriptors][memoryDescriptors].
 */
class MultiTapeTuringMachine(
    override val tapes: List<MultiTrackTapeDescriptor>
) : AbstractAutomaton(
    DISPLAY_NAME,
    memoryDescriptors = tapes,
    I18N.messages.getString("MultiTapeTuringMachine.Deterministic"),
    I18N.messages.getString("MultiTapeTuringMachine.Nondeterministic"),
    I18N.messages.getString("MultiTapeTuringMachine.Untitled")
), AutomatonWithTapes {
    init {
        require(tapes.isNotEmpty() && tapes.all { it.trackCount == 1 }) {
            "Illegal `tapes` argument when creating `MultiTapeTuringMachine`"
        }
    }

    override fun getTypeData() = MultiTapeTuringMachineData(tapes = tapes.map { it.getData() })

    override fun createEmptyAutomatonOfSameType() = MultiTapeTuringMachine(tapes)

    companion object {
        val DISPLAY_NAME: String = I18N.messages.getString("MultiTapeTuringMachine")
    }
}
