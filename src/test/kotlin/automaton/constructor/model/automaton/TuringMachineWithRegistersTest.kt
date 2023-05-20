package automaton.constructor.model.automaton

import automaton.constructor.model.memory.RegisterDescriptor
import automaton.constructor.model.memory.tape.MultiTrackTapeDescriptor
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class TuringMachineWithRegistersTest : AbstractAutomatonTest() {
    override fun createAutomaton() = TuringMachineWithRegisters(
        MultiTrackTapeDescriptor(trackCount = 1),
        List(3) { RegisterDescriptor() }
    )

    @Test
    fun `should fail to create TuringMachineWithRegisters with multiple tracks`() {
        assertThrows<IllegalArgumentException> {
            TuringMachineWithRegisters(
                MultiTrackTapeDescriptor(trackCount = 2),
                List(3) { RegisterDescriptor() }
            )
        }
    }

    @Test
    fun `should fail to create TuringMachineWithRegisters without registers`() {
        assertThrows<IllegalArgumentException> {
            TuringMachineWithRegisters(
                MultiTrackTapeDescriptor(trackCount = 1),
                registers = emptyList()
            )
        }
    }
}