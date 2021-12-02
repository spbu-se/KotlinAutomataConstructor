package automaton.constructor.model.factory

import automaton.constructor.model.memory.tape.MultiTrackTapeDescriptor

class TuringMachineFactory : AbstractAutomatonFactory("Turing machine") {
    override fun createMemoryDescriptors() = listOf(MultiTrackTapeDescriptor(1))

    override fun createSettings() = emptyList<Nothing>()
}
