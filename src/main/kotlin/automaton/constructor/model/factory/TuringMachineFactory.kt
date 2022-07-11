package automaton.constructor.model.factory

import automaton.constructor.model.automaton.TuringMachine
import automaton.constructor.model.automaton.TuringMachine.Companion.NAME

class TuringMachineFactory : AbstractAutomatonFactory(NAME) {
    override fun createAutomaton() = TuringMachine()
}
