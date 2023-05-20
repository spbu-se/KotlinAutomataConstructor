package automaton.constructor.model.module.layout

import automaton.constructor.model.TestAutomatons
import automaton.constructor.model.automaton.Automaton
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

abstract class AbstractLayoutTest {
    abstract fun assertGoodLayout(automaton: Automaton)
    abstract fun layout(automaton: Automaton)

    @ParameterizedTest
    @MethodSource("testLayoutSource")
    fun testLayout(automaton: Automaton) {
        layout(automaton)
        assertGoodLayout(automaton)
    }

    private fun testLayoutSource() = TestAutomatons.allAutomataStream()
}