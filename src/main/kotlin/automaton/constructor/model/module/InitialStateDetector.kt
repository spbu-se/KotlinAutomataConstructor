package automaton.constructor.model.module

import automaton.constructor.model.automaton.Automaton
import automaton.constructor.utils.filteredSet

private val initialStateDetectorFactory = { automaton: Automaton -> InitialStateDetector(automaton) }
val Automaton.initialStates get() = getModule(initialStateDetectorFactory).initialStates

class InitialStateDetector(automaton: Automaton) : AutomatonModule {
    val initialStates = automaton.states.filteredSet { it.isInitialProperty }
}
