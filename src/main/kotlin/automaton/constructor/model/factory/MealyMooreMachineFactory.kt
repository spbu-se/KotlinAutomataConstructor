package automaton.constructor.model.factory

import automaton.constructor.model.automaton.MealyMooreMachine
import automaton.constructor.model.memory.output.MealyMooreOutputTapeDescriptor
import automaton.constructor.model.memory.tape.InputTapeDescriptor

class MealyMooreMachineFactory : AbstractAutomatonFactory(MealyMooreMachine.DISPLAY_NAME) {
    override fun createAutomaton() = MealyMooreMachine(
        inputTape = InputTapeDescriptor(),
        mealyMooreOutputTape = MealyMooreOutputTapeDescriptor()
    )
}
