package automaton.constructor.model.module

import automaton.constructor.model.TestAutomatons
import automaton.constructor.model.memory.tape.InputTapeDescriptor
import automaton.constructor.model.property.FormalRegex
import automaton.constructor.utils.I18N
import automaton.constructor.utils.capitalize
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class AutomatonDescriptionProviderTest {
    @Test
    fun `bfs should be nondeterministic custom automaton`() =
        assertEquals(
            listOf(
                TestAutomatons.BFS.nondeterministicAdjective,
                I18N.messages.getString("CustomAutomaton")
            ).filter { it.isNotEmpty() }.joinToString(" ").capitalize(),
            TestAutomatons.BFS.description
        )

    @Test
    fun `binary-increment should be deterministic Turing machine`() =
        assertEquals(
            listOf(
                TestAutomatons.BINARY_INCREMENT.deterministicAdjective,
                I18N.messages.getString("TuringMachine")
            ).filter { it.isNotEmpty() }.joinToString(" ").capitalize(), TestAutomatons.BINARY_INCREMENT.description
        )

    @Test
    fun `empty-input-detector-with-epsilon-loop should be deterministic finite automaton with epsilon transitions`() =
        assertEquals(
            listOf(
                TestAutomatons.EMPTY_INPUT_DETECTOR_WITH_EPSILON_LOOP.deterministicAdjective,
                I18N.messages.getString("FiniteAutomaton"),
                I18N.messages.getString("AutomatonDescriptionProvider.WithEpsilonTransitions")
            ).filter { it.isNotEmpty() }.joinToString(" ").capitalize(),
            TestAutomatons.EMPTY_INPUT_DETECTOR_WITH_EPSILON_LOOP.description
        )

    @Test
    fun `even-palindromes should be nondeterministic pushdown automaton with epsilon transitions`() =
        assertEquals(
            listOf(
                TestAutomatons.EVEN_PALINDROMES.nondeterministicAdjective,
                I18N.messages.getString("PushdownAutomaton"),
                I18N.messages.getString("AutomatonDescriptionProvider.WithEpsilonTransitions")
            ).filter { it.isNotEmpty() }.joinToString(" ").capitalize(),
            TestAutomatons.EVEN_PALINDROMES.description
        )

    @Test
    fun `description should update when automaton is edited`() {
        val automaton = TestAutomatons.NO_STATES
        val expectedChar = (automaton.memoryDescriptors[0] as InputTapeDescriptor).expectedChar
        assertEquals(
            listOf(
                automaton.deterministicAdjective,
                I18N.messages.getString("FiniteAutomaton"),
                I18N.messages.getString("AutomatonDescriptionProvider.WithoutEpsilonTransitions")
            ).filter { it.isNotEmpty() }.joinToString(" ").capitalize(), automaton.description
        )
        val s0 = automaton.addState()
        val t0 = automaton.addTransition(s0, s0)
        assertEquals(
            listOf(
                automaton.deterministicAdjective,
                I18N.messages.getString("FiniteAutomaton"),
                I18N.messages.getString("AutomatonDescriptionProvider.WithEpsilonTransitions")
            ).filter { it.isNotEmpty() }.joinToString(" ").capitalize(), automaton.description
        )
        t0[expectedChar] = FormalRegex.fromString("0")
        assertEquals(
            listOf(
                automaton.deterministicAdjective,
                I18N.messages.getString("FiniteAutomaton"),
                I18N.messages.getString("AutomatonDescriptionProvider.WithoutEpsilonTransitions")
            ).filter { it.isNotEmpty() }.joinToString(" ").capitalize(), automaton.description
        )
        val t1 = automaton.addTransition(s0, s0)
        assertEquals(
            listOf(
                automaton.nondeterministicAdjective,
                I18N.messages.getString("FiniteAutomaton"),
                I18N.messages.getString("AutomatonDescriptionProvider.WithEpsilonTransitions")
            ).filter { it.isNotEmpty() }.joinToString(" ").capitalize(), automaton.description
        )
        t1[expectedChar] = FormalRegex.fromString("0")
        assertEquals(
            listOf(
                automaton.nondeterministicAdjective,
                I18N.messages.getString("FiniteAutomaton"),
                I18N.messages.getString("AutomatonDescriptionProvider.WithoutEpsilonTransitions")
            ).filter { it.isNotEmpty() }.joinToString(" ").capitalize(), automaton.description
        )
        automaton.removeTransition(t0)
        assertEquals(
            listOf(
                automaton.deterministicAdjective,
                I18N.messages.getString("FiniteAutomaton"),
                I18N.messages.getString("AutomatonDescriptionProvider.WithoutEpsilonTransitions")
            ).filter { it.isNotEmpty() }.joinToString(" ").capitalize(), automaton.description
        )
    }
}
