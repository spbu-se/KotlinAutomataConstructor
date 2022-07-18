package automaton.constructor.model.factory

import automaton.constructor.model.automaton.MealyMooreMachine
import automaton.constructor.model.memory.output.MealyMooreOutputTapeDescriptor
import automaton.constructor.model.module.tape.InputTapeDescriptor

class MealyMooreMachineFactory : AbstractAutomatonFactory(MealyMooreMachine.NAME) {
    override fun createAutomaton() = MealyMooreMachine(
        inputTape = InputTapeDescriptor(),
        mealyMooreOutputTape = MealyMooreOutputTapeDescriptor()
    )
}
