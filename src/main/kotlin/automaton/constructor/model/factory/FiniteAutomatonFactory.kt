package automaton.constructor.model.factory

import automaton.constructor.model.automaton.FiniteAutomaton
import automaton.constructor.model.module.tape.InputTapeDescriptor

class FiniteAutomatonFactory : AbstractAutomatonFactory(FiniteAutomaton.NAME) {
    override fun createAutomaton() = FiniteAutomaton(inputTape = InputTapeDescriptor())
}
