package automaton.constructor.model.module

import automaton.constructor.model.Automaton
import automaton.constructor.utils.filteredSet
import javafx.beans.binding.Bindings.isNotEmpty

val finalStateWithTransitionDetectorFactory = { automaton: Automaton -> FinalStateWithTransitionDetector(automaton) }
val Automaton.finalStatesWithTransitions get() = getModule(finalStateWithTransitionDetectorFactory).finalStatesWithTransitions

class FinalStateWithTransitionDetector(val automaton: Automaton) : AutomatonModule {
    val finalStatesWithTransitions = automaton.finalStates.filteredSet {
        isNotEmpty(automaton.getTransitions(it))
    }
}
