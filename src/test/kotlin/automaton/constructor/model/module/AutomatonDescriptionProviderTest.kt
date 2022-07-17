package automaton.constructor.model.module

import automaton.constructor.model.TestAutomatons
import automaton.constructor.model.memory.tape.InputTapeDescriptor
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class AutomatonDescriptionProviderTest {
    @Test
    fun `bfs should be nondeterministic bfs`() =
        assertEquals(
            "Nondeterministic custom automaton",
            TestAutomatons.BFS.description
        )

    @Test
    fun `binary-increment should be deterministic Turing machine`() =
        assertEquals("Deterministic Turing machine", TestAutomatons.BINARY_INCREMENT.description)

    @Test
    fun `empty-input-detector-with-epsilon-loop should be deterministic finite automaton with epsilon transitions`() =
        assertEquals(
            "Deterministic finite automaton with epsilon transitions",
            TestAutomatons.EMPTY_INPUT_DETECTOR_WITH_EPSILON_LOOP.description
        )

    @Test
    fun `even-palindromes should be nondeterministic pushdown automaton with epsilon transitions`() =
        assertEquals(
            "Nondeterministic pushdown automaton with epsilon transitions",
            TestAutomatons.EVEN_PALINDROMES.description
        )

    @Test
    fun `description should update when automaton is edited`() {
        val automaton = TestAutomatons.NO_STATES
        val expectedChar = (automaton.memoryDescriptors[0] as InputTapeDescriptor).expectedChar
        assertEquals("Deterministic finite automaton without epsilon transitions", automaton.description)
        val s0 = automaton.addState()
        val t0 = automaton.addTransition(s0, s0)
        assertEquals("Deterministic finite automaton with epsilon transitions", automaton.description)
        t0[expectedChar] = '0'
        assertEquals("Deterministic finite automaton without epsilon transitions", automaton.description)
        val t1 = automaton.addTransition(s0, s0)
        assertEquals("Nondeterministic finite automaton with epsilon transitions", automaton.description)
        t1[expectedChar] = '0'
        assertEquals("Nondeterministic finite automaton without epsilon transitions", automaton.description)
        automaton.removeTransition(t0)
        assertEquals("Deterministic finite automaton without epsilon transitions", automaton.description)
    }
}
