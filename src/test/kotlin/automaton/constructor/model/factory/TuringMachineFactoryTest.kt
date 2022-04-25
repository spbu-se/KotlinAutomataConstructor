package automaton.constructor.model.factory

import automaton.constructor.model.memory.tape.MultiTrackTapeDescriptor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TuringMachineFactoryTest {
    @Test
    fun `should create Turing machine`() {
        val automaton = TuringMachineFactory().createAutomaton()
        assertEquals(1, automaton.memoryDescriptors.size)
        val tape = automaton.memoryDescriptors[0]
        assertTrue(tape is MultiTrackTapeDescriptor)
        assertEquals(1, tape.trackCount)
    }
}
