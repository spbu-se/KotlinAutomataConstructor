package automaton.constructor.model.automaton

import automaton.constructor.model.memory.tape.InputTapeDescriptor
import automaton.constructor.model.memory.tape.MultiTrackTapeDescriptor

class CustomAutomatonTest : AbstractAutomatonTest() {
    override fun createAutomaton() = CustomAutomaton(
        memoryDescriptors = listOf(InputTapeDescriptor(), MultiTrackTapeDescriptor(trackCount = 2))
    )
}