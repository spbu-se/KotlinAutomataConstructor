package automaton.constructor.model.factory

import automaton.constructor.model.automaton.MealyMooreMachine
import automaton.constructor.model.memory.output.MealyMooreOutputDescriptor
import automaton.constructor.model.memory.tape.InputTapeDescriptor

class MealyMooreMachineFactory : AbstractAutomatonFactory(MealyMooreMachine.NAME) {
    override fun createAutomaton() = MealyMooreMachine(
        inputTape = InputTapeDescriptor(),
        mealyMooreOutput = MealyMooreOutputDescriptor()
    )
}
