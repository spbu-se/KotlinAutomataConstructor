package automaton.constructor.model.automaton

import automaton.constructor.model.automaton.flavours.AutomatonWithRegisters
import automaton.constructor.model.automaton.flavours.AutomatonWithTape
import automaton.constructor.model.data.TuringMachineWithRegistersData
import automaton.constructor.model.memory.RegisterDescriptor
import automaton.constructor.model.memory.tape.MultiTrackTapeDescriptor
import automaton.constructor.utils.I18N.messages

/**
 * Turing machine with registers.
 *
 * It's an automaton with a [tape] and several [registers] as [memory descriptors][memoryDescriptors].
 */
class TuringMachineWithRegisters(
    override val tape: MultiTrackTapeDescriptor,
    override val registers: List<RegisterDescriptor>
) : AbstractAutomaton(DISPLAY_NAME, memoryDescriptors = listOf(tape) + registers),
    AutomatonWithTape, AutomatonWithRegisters {
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
        val DISPLAY_NAME: String = messages.getString("TuringMachineWithRegisters")
    }
}
