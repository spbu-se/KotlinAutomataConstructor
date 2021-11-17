package automaton.constructor.model.module

import automaton.constructor.model.Automaton
import automaton.constructor.utils.filteredSet

val initialStateDetectorFactory = { automaton: Automaton -> InitialStateDetector(automaton) }
val Automaton.initialStates get() = getModule(initialStateDetectorFactory).initialStates

class InitialStateDetector(automaton: Automaton) : AutomatonModule {
    val initialStates = automaton.states.filteredSet { it.isInitialProperty }
}
