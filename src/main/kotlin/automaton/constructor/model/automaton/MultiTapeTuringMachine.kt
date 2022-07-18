package automaton.constructor.model.automaton

import automaton.constructor.model.data.MultiTapeTuringMachineData
import automaton.constructor.model.memory.tape.MultiTrackTapeDescriptor
import automaton.constructor.utils.I18N.messages

/**
 * Multi-tape Turing machine.
 *
 * It's an automaton with several [tapes] as [memory descriptors][memoryDescriptors].
 */
class MultiTapeTuringMachine(
    val tapes: List<MultiTrackTapeDescriptor>
) : AbstractAutomaton(DISPLAY_NAME, memoryDescriptors = tapes) {
    init {
        require(tapes.isNotEmpty() && tapes.all { it.trackCount == 1 }) {
            messages.getString("MultiTapeTuringMachine.IllegalTapesArgument")
        }
    }

    override fun getTypeData() = MultiTapeTuringMachineData(
        tapes = tapes.map { it.getData() }
    )

    companion object {
        val DISPLAY_NAME: String = messages.getString("MultiTapeTuringMachine")
    }
}
