package automaton.constructor.model.factory

import automaton.constructor.model.module.tape.InputTapeDescriptor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FiniteAutomatonFactoryTest {
    @Test
    fun `should create finite automaton`() {
        val automaton = FiniteAutomatonFactory().createAutomaton()
        assertEquals(1, automaton.memoryDescriptors.size)
        assertTrue(automaton.memoryDescriptors[0] is InputTapeDescriptor)
    }
}
