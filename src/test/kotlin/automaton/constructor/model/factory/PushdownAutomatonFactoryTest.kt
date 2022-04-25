package automaton.constructor.model.factory

import automaton.constructor.model.memory.StackDescriptor
import automaton.constructor.model.memory.tape.InputTapeDescriptor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PushdownAutomatonFactoryTest {
    @Test
    fun `when stackCount is 4 should create pushdown automaton with 4 stacks`() {
        val automaton = PushdownAutomatonFactory().apply { stackCount = 4 }.createAutomaton()
        assertEquals(5, automaton.memoryDescriptors.size)
        assertTrue(automaton.memoryDescriptors[0] is InputTapeDescriptor)
        automaton.memoryDescriptors.drop(1).forEach { assertTrue(it is StackDescriptor) }
    }
}
