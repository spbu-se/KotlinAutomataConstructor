package automaton.constructor.model.factory

import automaton.constructor.model.automaton.MooreMachine
import automaton.constructor.model.automaton.MooreMachine.Companion.NAME

class MooreMachineFactory : AbstractAutomatonFactory(NAME) {
    override fun createAutomaton() = MooreMachine()
}
