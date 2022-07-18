package automaton.constructor.model.factory

import automaton.constructor.model.memory.RegisterDescriptor
import automaton.constructor.model.memory.tape.MultiTrackTapeDescriptor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TuringMachineWithRegistersFactoryTest {
    @Test
    fun `when registerCount is 4 should create Turing machine with 4 registers`() {
        val automaton = TuringMachineWithRegistersFactory().apply { registerCount = 4 }.createAutomaton()
        assertEquals(5, automaton.memoryDescriptors.size)
        val tape = automaton.memoryDescriptors[0]
        assertTrue(tape is MultiTrackTapeDescriptor)
        assertEquals(1, tape.trackCount)
        automaton.memoryDescriptors.drop(1).forEach { assertTrue(it is RegisterDescriptor) }
    }
}
