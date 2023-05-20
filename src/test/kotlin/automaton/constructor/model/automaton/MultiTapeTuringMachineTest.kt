package automaton.constructor.model.automaton

import automaton.constructor.model.memory.tape.MultiTrackTapeDescriptor
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class MultiTapeTuringMachineTest : AbstractAutomatonTest() {
    override fun createAutomaton() = MultiTapeTuringMachine(List(3) { MultiTrackTapeDescriptor(trackCount = 1) })

    @Test
    fun `should fail to create MultiTapeTuringMachine with multi-track tapes`() {
        assertThrows<IllegalArgumentException> {
            MultiTapeTuringMachine(List(3) { MultiTrackTapeDescriptor(trackCount = 2) })
        }
    }

    @Test
    fun `should fail to create MultiTapeTuringMachine without tapes`() {
        assertThrows<IllegalArgumentException> {
            MultiTapeTuringMachine(tapes = emptyList())
        }
    }
}