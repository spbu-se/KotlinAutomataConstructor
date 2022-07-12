package automaton.constructor.model.module

import automaton.constructor.model.automaton.Automaton
import automaton.constructor.utils.filteredSet

val finalStateDetectorFactory = { automaton: Automaton -> FinalStateDetector(automaton) }
val Automaton.finalStates get() = getModule(finalStateDetectorFactory).finalStates

class FinalStateDetector(automaton: Automaton) : AutomatonModule {
    val finalStates = automaton.states.filteredSet { it.isFinalProperty }
}
