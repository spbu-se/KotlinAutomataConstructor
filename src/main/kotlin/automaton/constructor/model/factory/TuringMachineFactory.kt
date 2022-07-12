package automaton.constructor.model.factory

import automaton.constructor.model.automaton.TuringMachine
import automaton.constructor.model.memory.tape.MultiTrackTapeDescriptor

class TuringMachineFactory : AbstractAutomatonFactory(TuringMachine.NAME) {
    override fun createAutomaton() = TuringMachine(tape = MultiTrackTapeDescriptor(trackCount = 1))
}
