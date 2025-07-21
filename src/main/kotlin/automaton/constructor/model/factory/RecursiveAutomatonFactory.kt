package automaton.constructor.model.factory

import automaton.constructor.model.automaton.RecursiveAutomaton
import automaton.constructor.model.memory.tape.InputTapeDescriptor

class RecursiveAutomatonFactory : AbstractAutomatonFactory(RecursiveAutomaton.DISPLAY_NAME) {
    override fun createAutomaton() = RecursiveAutomaton(inputTape = InputTapeDescriptor())

    override fun createSettings() = TODO("Not yet implemented")
}