package automaton.constructor.model.factory

import automaton.constructor.model.memory.output.MealyOutputDescriptor
import automaton.constructor.model.memory.tape.InputTapeDescriptor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MealyMachineFactoryTest {
    @Test
    fun `should create Mealy machine`() {
        val automaton = MealyMachineFactory().createAutomaton()
        assertEquals(2, automaton.memoryDescriptors.size)
        assertTrue(automaton.memoryDescriptors[0] is InputTapeDescriptor)
        assertTrue(automaton.memoryDescriptors[1] is MealyOutputDescriptor)
    }
}
