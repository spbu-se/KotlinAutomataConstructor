package automaton.constructor.model.factory

import automaton.constructor.model.TestAutomatons
import automaton.constructor.model.data.getData
import automaton.constructor.model.memory.tape.InputTapeDescriptor
import org.junit.jupiter.api.assertThrows
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

    @Test
    fun `should create finite automaton from valid regex`() {
        val automaton = FiniteAutomatonFactory().apply { regex = "đ((S|$)ā)*(S|$)(a(S|$))*d" }.createAutomaton()
        assertEquals(1, automaton.memoryDescriptors.size)
        assertTrue(automaton.memoryDescriptors[0] is InputTapeDescriptor)
        assertEquals(TestAutomatons.C_ALIAS_REGEX.getData(), automaton.getData())
    }

    @Test
    fun `should throw AutomatonCreationFailedException for invalid regex`() {
        val factory = FiniteAutomatonFactory().apply { regex = "(a" }
        assertThrows<AutomatonCreationFailedException> { factory.createAutomaton() }
    }
}
