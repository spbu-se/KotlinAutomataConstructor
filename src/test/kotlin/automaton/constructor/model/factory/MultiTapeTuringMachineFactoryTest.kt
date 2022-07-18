package automaton.constructor.model.factory

import automaton.constructor.model.module.tape.MultiTrackTapeDescriptor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MultiTapeTuringMachineFactoryTest {
    @Test
    fun `when tapeCount is 4 should create Turing machine with 4 tapes`() {
        val automaton = MultiTapeTuringMachineFactory().apply { tapeCount = 4 }.createAutomaton()
        assertEquals(4, automaton.memoryDescriptors.size)
        automaton.memoryDescriptors.forEach {
            assertTrue(it is MultiTrackTapeDescriptor)
            assertEquals(1, it.trackCount)
        }
    }
}
