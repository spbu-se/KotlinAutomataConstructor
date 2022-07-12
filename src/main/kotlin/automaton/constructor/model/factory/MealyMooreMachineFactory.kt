package automaton.constructor.model.factory

import automaton.constructor.model.automaton.MealyMooreMachine
import automaton.constructor.model.memory.output.MealyOutputDescriptor
import automaton.constructor.model.memory.output.MooreOutputDescriptor
import automaton.constructor.model.memory.tape.InputTapeDescriptor

class MealyMooreMachineFactory : AbstractAutomatonFactory(MealyMooreMachine.NAME) {
    override fun createAutomaton() = MealyMooreMachine(
        inputTape = InputTapeDescriptor(),
        mealyOutput = MealyOutputDescriptor(),
        mooreOutput = MooreOutputDescriptor()
    )
}
