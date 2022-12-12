package automaton.constructor.model.module.executor

import automaton.constructor.model.factory.FiniteAutomatonFactory
import automaton.constructor.model.factory.TuringMachineFactory
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SteppingStrategyTest {
    @Test
    fun `StepOverStrategy should be available for Turing machine`() {
        assertTrue(StepOverStrategy.isAvailableFor(TuringMachineFactory().createAutomaton()))
    }

    @Test
    fun `StepOverStrategy should not be available for finite automaton`() {
        assertFalse(StepOverStrategy.isAvailableFor(FiniteAutomatonFactory().createAutomaton()))
    }

    @Test
    fun `StepIntoStrategy should be available for Turing machine`() {
        assertTrue(StepIntoStrategy.isAvailableFor(TuringMachineFactory().createAutomaton()))
    }

    @Test
    fun `StepIntoStrategy should not be available for finite automaton`() {
        assertFalse(StepIntoStrategy.isAvailableFor(FiniteAutomatonFactory().createAutomaton()))
    }

    @Test
    fun `StepByStateStrategy should be available for finite automaton`() {
        assertTrue(StepByStateStrategy.isAvailableFor(FiniteAutomatonFactory().createAutomaton()))
    }

    @Test
    fun `StepByStateStrategy should not be available for Turing machine`() {
        assertFalse(StepByStateStrategy.isAvailableFor(TuringMachineFactory().createAutomaton()))
    }

    @Test
    fun `StepByClosureStrategy should be available for finite automaton`() {
        assertTrue(StepByClosureStrategy.isAvailableFor(FiniteAutomatonFactory().createAutomaton()))
    }

    @Test
    fun `StepByClosureStrategy should not be available for Turing machine`() {
        assertFalse(StepByClosureStrategy.isAvailableFor(TuringMachineFactory().createAutomaton()))
    }
}
