package automaton.constructor.model.factory

import automaton.constructor.model.memory.output.MooreOutputDescriptor
import automaton.constructor.model.memory.tape.InputTapeDescriptor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MooreMachineFactoryTest {
    @Test
    fun `should create Moore machine`() {
        val automaton = MooreMachineFactory().createAutomaton()
        assertEquals(2, automaton.memoryDescriptors.size)
        assertTrue(automaton.memoryDescriptors[0] is InputTapeDescriptor)
        assertTrue(automaton.memoryDescriptors[1] is MooreOutputDescriptor)
    }
}
