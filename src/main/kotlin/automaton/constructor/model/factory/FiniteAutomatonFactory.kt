package automaton.constructor.model.factory

import automaton.constructor.model.automaton.FiniteAutomaton
import automaton.constructor.model.automaton.FiniteAutomaton.Companion.NAME

class FiniteAutomatonFactory : AbstractAutomatonFactory(NAME) {
    override fun createAutomaton() = FiniteAutomaton()
}
