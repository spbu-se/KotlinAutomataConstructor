package automaton.constructor.model.automaton

import automaton.constructor.model.memory.tape.InputTapeDescriptor

class FiniteAutomatonTest : AbstractAutomatonTest() {
    override fun createAutomaton() = FiniteAutomaton(InputTapeDescriptor())
}