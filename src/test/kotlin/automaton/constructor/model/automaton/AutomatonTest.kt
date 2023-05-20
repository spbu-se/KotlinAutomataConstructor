package automaton.constructor.model.automaton

import automaton.constructor.model.TestAutomatons
import automaton.constructor.model.factory.FiniteAutomatonFactory
import automaton.constructor.model.transformation.AutomatonTransformation
import io.mockk.every
import io.mockk.mockk
import javafx.geometry.Point2D
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame

class AutomatonTest {
    @Test
    fun `copyAndAddState should retain original field values when no new value are provided`() {
        val automaton = FiniteAutomatonFactory().createAutomaton()
        val oldState = automaton.addState()
        oldState.name = "S0"
        oldState.position = Point2D(101.0, 203.0)
        oldState.isInitial = false
        oldState.isFinal = false
        val newState = automaton.copyAndAddState(oldState)
        assertEquals(oldState.name, newState.name)
        assertEquals(oldState.position, newState.position)
        assertEquals(oldState.isInitial, newState.isInitial)
        assertEquals(oldState.isFinal, newState.isFinal)
    }

    @Test
    fun `copyAndAddState should override every field when new value is provided for every field`() {
        val automaton = FiniteAutomatonFactory().createAutomaton()
        val oldState = automaton.addState()
        val newName = "new name"
        val newPosition = Point2D(101.0, 203.0)
        val newIsInitial = true
        val newIsFinal = true
        val newState = automaton.copyAndAddState(
            oldState,
            newName = newName,
            newPosition = newPosition,
            newIsInitial = newIsInitial,
            newIsFinal = newIsFinal
        )
        assertEquals(newName, newState.name)
        assertEquals(newPosition, newState.position)
        assertEquals(newIsInitial, newState.isInitial)
        assertEquals(newIsFinal, newState.isFinal)
    }

    @Test
    fun `copyAndAddTransitionConditionally should be able to duplicate epsilon loop when no conditions are specified`() {
        val automaton = TestAutomatons.EMPTY_INPUT_DETECTOR_WITH_EPSILON_LOOP
        assertNotNull(automaton.copyAndAddTransitionConditionally(
            automaton.transitions.first { it.isPure() }
        ))
    }

    @Test
    fun `copyAndAddTransitionConditionally should fail to duplicate epsilon loop when any condition is specified`() {
        val automaton = TestAutomatons.EMPTY_INPUT_DETECTOR_WITH_EPSILON_LOOP
        assertNull(automaton.copyAndAddTransitionConditionally(
            automaton.transitions.first { it.isPure() }, ignoreIfCopyAlreadyExists = true
        ))
        assertNull(automaton.copyAndAddTransitionConditionally(
            automaton.transitions.first { it.isPure() }, ignoreIfCopyIsPureLoop = true
        ))
        assertNull(automaton.copyAndAddTransitionConditionally(
            automaton.transitions.first { it.isPure() }, ignoreIfTransitionIsPureLoop = true
        ))
    }

    @Test
    fun `transformationOutput should be null when transformation is not set`() {
        val automaton = TestAutomatons.EMPTY_INPUT_DETECTOR_WITH_EPSILON_LOOP
        assertNull(automaton.transformationOutput)
    }


    @Test
    fun `transformationOutput should be set when transformation is set`() {
        val automaton = TestAutomatons.EMPTY_INPUT_DETECTOR_WITH_EPSILON_LOOP
        val transformation = mockk<AutomatonTransformation>()
        val resultingAutomaton = mockk<Automaton>()
        every { transformation.resultingAutomaton } returns resultingAutomaton
        automaton.isInputForTransformation = transformation
        assertSame(resultingAutomaton, automaton.transformationOutput)
    }
}
