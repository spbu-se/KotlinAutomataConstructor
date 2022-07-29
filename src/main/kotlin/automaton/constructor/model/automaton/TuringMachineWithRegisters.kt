package automaton.constructor.model.automaton

import automaton.constructor.model.automaton.flavours.AutomatonWithRegisters
import automaton.constructor.model.automaton.flavours.AutomatonWithTape
import automaton.constructor.model.data.TuringMachineWithRegistersData
import automaton.constructor.model.memory.RegisterDescriptor
import automaton.constructor.model.memory.tape.MultiTrackTapeDescriptor
import automaton.constructor.utils.I18N

/**
 * Turing machine with registers.
 *
 * It's an automaton with a [tape] and several [registers] as [memory descriptors][memoryDescriptors].
 */
class TuringMachineWithRegisters(
    override val tape: MultiTrackTapeDescriptor, override val registers: List<RegisterDescriptor>
) : AbstractAutomaton(
    DISPLAY_NAME,
    memoryDescriptors = listOf(tape) + registers,
    I18N.messages.getString("TuringMachineWithRegisters.Deterministic"),
    I18N.messages.getString("TuringMachineWithRegisters.Nondeterministic"),
    I18N.messages.getString("TuringMachineWithRegisters.Untitled")
), AutomatonWithTape, AutomatonWithRegisters {
    init {
        require(tape.trackCount == 1) {
            "Illegal `tape` argument when creating `TuringMachineWithRegisters`"
        }
        require(registers.isNotEmpty()) {
            "Illegal `registers` argument when creating `TuringMachineWithRegisters`"
        }
    }

    override fun getTypeData() =
        TuringMachineWithRegistersData(tape = tape.getData(), registers = registers.map { it.getData() })

    companion object {
        val DISPLAY_NAME: String = I18N.messages.getString("TuringMachineWithRegisters")
    }
}
