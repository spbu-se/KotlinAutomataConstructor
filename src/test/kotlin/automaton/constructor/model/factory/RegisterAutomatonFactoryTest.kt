package automaton.constructor.model.factory

import automaton.constructor.model.memory.RegisterDescriptor
import automaton.constructor.model.memory.tape.InputTapeDescriptor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegisterAutomatonFactoryTest {
    @Test
    fun `when registerCount is 4 should create register automaton with 4 registers`() {
        val automaton = RegisterAutomatonFactory().apply { registerCount = 4 }.createAutomaton()
        assertEquals(5, automaton.memoryDescriptors.size)
        assertTrue(automaton.memoryDescriptors[0] is InputTapeDescriptor)
        automaton.memoryDescriptors.drop(1).forEach { assertTrue(it is RegisterDescriptor) }
    }
}
