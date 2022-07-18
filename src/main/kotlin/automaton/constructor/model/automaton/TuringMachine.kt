package automaton.constructor.model.automaton

import automaton.constructor.model.data.TuringMachineData
import automaton.constructor.model.module.tape.MultiTrackTapeDescriptor
import automaton.constructor.utils.I18N.messages

/**
 * Turing machine.
 *
 * It's an automaton with a [tape] as a [memory descriptor][memoryDescriptors].
 */
class TuringMachine(
    val tape: MultiTrackTapeDescriptor
) : AbstractAutomaton(NAME, memoryDescriptors = listOf(tape)) {
    init {
        require(tape.trackCount == 1) {
            messages.getString("TuringMachine.IllegalTapeArgument")
        }
    }

    override fun getTypeData() = TuringMachineData(
        tape = tape.getData()
    )

    companion object {
        const val NAME = "Turing machine"
    }
}
