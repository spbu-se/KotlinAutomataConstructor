package automaton.constructor.model.factory

import automaton.constructor.model.memory.output.MealyMooreOutputTapeDescriptor
import automaton.constructor.model.memory.tape.InputTapeDescriptor
import kotlin.test.*

class MealyMooreMachineFactoryTest {
    @Test
    fun `should create Mealy-Moore machine`() {
        val automaton = MealyMooreMachineFactory().createAutomaton()
        assertEquals(2, automaton.memoryDescriptors.size)
        assertTrue(automaton.memoryDescriptors[0] is InputTapeDescriptor)
        assertTrue(automaton.memoryDescriptors[1] is MealyMooreOutputTapeDescriptor)
    }
}
