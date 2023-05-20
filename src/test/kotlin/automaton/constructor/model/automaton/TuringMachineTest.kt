package automaton.constructor.model.automaton

import automaton.constructor.model.memory.tape.MultiTrackTapeDescriptor
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class TuringMachineTest : AbstractAutomatonTest() {
    override fun createAutomaton() = TuringMachine(MultiTrackTapeDescriptor(trackCount = 1))

    @Test
    fun `should fail to create simple TuringMachine with multiple tracks`() {
        assertThrows<IllegalArgumentException> {
            TuringMachine(MultiTrackTapeDescriptor(trackCount = 2))
        }
    }
}