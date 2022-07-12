package automaton.constructor.model.factory

import automaton.constructor.model.memory.output.MealyOutputDescriptor
import automaton.constructor.model.memory.output.MooreOutputDescriptor
import automaton.constructor.model.memory.tape.InputTapeDescriptor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MealyMooreMachineFactoryTest {
    @Test
    fun `should create Mealy-Moore machine`() {
        val automaton = MealyMooreMachineFactory().createAutomaton()
        assertEquals(3, automaton.memoryDescriptors.size)
        assertTrue(automaton.memoryDescriptors[0] is InputTapeDescriptor)
        assertTrue(automaton.memoryDescriptors[1] is MealyOutputDescriptor)
        assertTrue(automaton.memoryDescriptors[2] is MooreOutputDescriptor)
    }
}
