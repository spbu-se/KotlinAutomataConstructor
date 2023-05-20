package automaton.constructor.model.automaton

import automaton.constructor.model.memory.StackDescriptor
import automaton.constructor.model.memory.tape.InputTapeDescriptor
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class PushdownAutomatonTest : AbstractAutomatonTest() {
    override fun createAutomaton() = PushdownAutomaton(InputTapeDescriptor(), List(2) { StackDescriptor() })

    @Test
    fun `should fail to create PushdownAutomaton without stacks`() {
        assertThrows<IllegalArgumentException> {
            PushdownAutomaton(InputTapeDescriptor(), stacks = emptyList())
        }
    }
}