package automaton.constructor.model.factory

import automaton.constructor.model.automaton.MooreMachine
import automaton.constructor.model.memory.output.MooreOutputDescriptor
import automaton.constructor.model.memory.tape.InputTapeDescriptor

class MooreMachineFactory : AbstractAutomatonFactory(MooreMachine.NAME) {
    override fun createAutomaton() = MooreMachine(
        inputTape = InputTapeDescriptor(),
        mooreOutput = MooreOutputDescriptor()
    )
}
