package automaton.constructor.model.automaton

import automaton.constructor.model.automaton.flavours.AutomatonWithTape
import automaton.constructor.model.data.TuringMachineData
import automaton.constructor.model.memory.tape.MultiTrackTapeDescriptor
import automaton.constructor.utils.I18N

/**
 * Turing machine.
 *
 * It's an automaton with a [tape] as a [memory descriptor][memoryDescriptors].
 */
class TuringMachine(
    override val tape: MultiTrackTapeDescriptor
) : AbstractAutomaton(
    DISPLAY_NAME,
    memoryDescriptors = listOf(tape),
    I18N.messages.getString("TuringMachine.Deterministic"),
    I18N.messages.getString("TuringMachine.Nondeterministic"),
    I18N.messages.getString("TuringMachine.Untitled")
), AutomatonWithTape {
    init {
        require(tape.trackCount == 1) {
            "Illegal `tape` argument when creating `TuringMachine`"
        }
    }

    override fun getTypeData() = TuringMachineData(
        tape = tape.getData()
    )

    companion object {
        val DISPLAY_NAME: String = I18N.messages.getString("TuringMachine")
    }
}
