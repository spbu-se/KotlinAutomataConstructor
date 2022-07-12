package automaton.constructor.model.factory

import automaton.constructor.model.automaton.MealyMachine
import automaton.constructor.model.memory.output.MealyOutputDescriptor
import automaton.constructor.model.memory.tape.InputTapeDescriptor

class MealyMachineFactory : AbstractAutomatonFactory(MealyMachine.NAME) {
    override fun createAutomaton() = MealyMachine(
        inputTape = InputTapeDescriptor(),
        mealyOutput = MealyOutputDescriptor()
    )
}
