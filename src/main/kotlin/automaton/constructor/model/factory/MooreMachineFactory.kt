package automaton.constructor.model.factory

import automaton.constructor.model.memory.output.MooreOutputDescriptor
import automaton.constructor.model.memory.tape.InputTapeDescriptor

class MooreMachineFactory : AbstractAutomatonFactory("Moore machine") {
    override fun createMemoryDescriptors() = listOf(InputTapeDescriptor(), MooreOutputDescriptor())

    override fun createSettings() = emptyList<Nothing>()
}
