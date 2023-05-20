package automaton.constructor.model.automaton

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

abstract class AbstractAutomatonTest {
    @Test
    fun `createEmptyAutomatonOfSameType should create automaton of same class`() {
        val automaton = createAutomaton()
        assertSame(
            automaton::class,
            automaton.createEmptyAutomatonOfSameType()::class
        )
    }

    @Test
    fun `createEmptyAutomatonOfSameType should create automaton with equal type data`() {
        val automaton = createAutomaton()
        assertEquals(
            automaton.getTypeData(),
            automaton.createEmptyAutomatonOfSameType().getTypeData()
        )
    }

    protected abstract fun createAutomaton(): Automaton
}