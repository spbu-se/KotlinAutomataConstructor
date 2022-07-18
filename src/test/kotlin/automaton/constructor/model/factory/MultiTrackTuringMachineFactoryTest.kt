package automaton.constructor.model.factory

import automaton.constructor.model.memory.tape.MultiTrackTapeDescriptor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MultiTrackTuringMachineFactoryTest {
    @Test
    fun `when trackCount is 4 should create Turing machine with 4 tracks`() {
        val automaton = MultiTrackTuringMachineFactory().apply { trackCount = 4 }.createAutomaton()
        assertEquals(1, automaton.memoryDescriptors.size)
        val tape = automaton.memoryDescriptors[0]
        assertTrue(tape is MultiTrackTapeDescriptor)
        assertEquals(4, tape.trackCount)
    }
}
