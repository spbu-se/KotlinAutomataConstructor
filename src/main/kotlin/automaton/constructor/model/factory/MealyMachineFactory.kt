package automaton.constructor.model.factory

import automaton.constructor.model.automaton.MealyMachine
import automaton.constructor.model.automaton.MealyMachine.Companion.NAME

class MealyMachineFactory : AbstractAutomatonFactory(NAME) {
    override fun createAutomaton() = MealyMachine()
}
