package automaton.constructor.model.automaton

import automaton.constructor.model.memory.RegisterDescriptor
import automaton.constructor.model.memory.tape.InputTapeDescriptor
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class RegisterAutomatonTest : AbstractAutomatonTest() {
    override fun createAutomaton() = RegisterAutomaton(InputTapeDescriptor(), List(4) { RegisterDescriptor() })

    @Test
    fun `should fail to create RegisterAutomaton without stacks`() {
        assertThrows<IllegalArgumentException> {
            RegisterAutomaton(InputTapeDescriptor(), registers = emptyList())
        }
    }
}