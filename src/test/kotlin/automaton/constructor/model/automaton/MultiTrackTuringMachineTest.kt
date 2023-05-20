package automaton.constructor.model.automaton

import automaton.constructor.model.memory.tape.MultiTrackTapeDescriptor
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class MultiTrackTuringMachineTest : AbstractAutomatonTest() {
    override fun createAutomaton() = MultiTrackTuringMachine(MultiTrackTapeDescriptor(trackCount = 3))

    @Test
    fun `should fail to create MultiTrackTuringMachine without tracks`() {
        assertThrows<IllegalArgumentException> {
            MultiTrackTuringMachine(tracks = MultiTrackTapeDescriptor(trackCount = 0))
        }
    }
}