package automaton.constructor.model.automaton

import automaton.constructor.model.memory.tape.MultiTrackTapeDescriptor
import automaton.constructor.utils.I18N.labels

/**
 * Turing machine.
 *
 * It's an automaton with a [tape] as a [memory descriptor][memoryDescriptors].
 */
class TuringMachine(
    val tape: MultiTrackTapeDescriptor
) : Automaton by BaseAutomaton(NAME, memoryDescriptors = listOf(tape)) {
    init {
        require(tape.trackCount == 1) {
            labels.getString("TuringMachine.IllegalTapeArgument")
        }
    }

    companion object {
        val NAME: String = labels.getString("TuringMachine.NAME")
    }
}
