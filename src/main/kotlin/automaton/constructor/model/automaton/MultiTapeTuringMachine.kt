package automaton.constructor.model.automaton

import automaton.constructor.model.memory.tape.MultiTrackTapeDescriptor
import automaton.constructor.utils.I18N.messages

/**
 * Multi-tape Turing machine.
 *
 * It's an automaton with several [tapes] as [memory descriptors][memoryDescriptors].
 */
class MultiTapeTuringMachine(
    val tapes: List<MultiTrackTapeDescriptor>
) : Automaton by BaseAutomaton(NAME, memoryDescriptors = tapes) {
    init {
        require(tapes.isNotEmpty() && tapes.all { it.trackCount == 1 }) {
            messages.getString("MultiTapeTuringMachine.IllegalTapesArgument")
        }
    }

    companion object {
        val NAME: String = messages.getString("MultiTapeTuringMachine")
    }
}
