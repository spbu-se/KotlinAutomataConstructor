package automaton.constructor.model.factory

import automaton.constructor.model.memory.tape.InputTapeDescriptor

class FiniteAutomatonFactory : AbstractAutomatonFactory("finite automaton") {
    override fun createMemoryDescriptors() = listOf(InputTapeDescriptor())

    override fun createSettings() = emptyList<Nothing>()
}
