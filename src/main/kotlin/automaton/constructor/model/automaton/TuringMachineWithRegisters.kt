package automaton.constructor.model.automaton

import automaton.constructor.model.data.TuringMachineWithRegistersData
import automaton.constructor.model.memory.RegisterDescriptor
import automaton.constructor.model.module.tape.MultiTrackTapeDescriptor
import automaton.constructor.utils.I18N.messages

/**
 * Turing machine with registers.
 *
 * It's an automaton with a [tape] and several [registers] as [memory descriptors][memoryDescriptors].
 */
class TuringMachineWithRegisters(
    val tape: MultiTrackTapeDescriptor,
    val registers: List<RegisterDescriptor>
) : AbstractAutomaton(NAME, memoryDescriptors = listOf(tape) + registers) {
    init {
        require(tape.trackCount == 1) {
            messages.getString("TuringMachineWithRegisters.IllegalTapeArgument")
        }
        require(registers.isNotEmpty()) {
            messages.getString("TuringMachineWithRegisters.IllegalRegistersArgument")
        }
    }

    override fun getTypeData() = TuringMachineWithRegistersData(
        tape = tape.getData(),
        registers = registers.map { it.getData() }
    )

    companion object {
        const val NAME = "Turing machine with registers"
    }
}
