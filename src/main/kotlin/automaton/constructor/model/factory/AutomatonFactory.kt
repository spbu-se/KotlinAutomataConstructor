package automaton.constructor.model.factory

import automaton.constructor.model.Automaton
import automaton.constructor.utils.Editable

interface AutomatonFactory : Editable {
    fun createAutomaton(): Automaton
}
