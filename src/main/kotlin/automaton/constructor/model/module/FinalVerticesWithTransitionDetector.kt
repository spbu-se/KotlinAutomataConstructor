package automaton.constructor.model.module

import automaton.constructor.model.automaton.Automaton
import automaton.constructor.utils.filteredSet
import javafx.beans.binding.Bindings.isNotEmpty

private val finalVerticesWithTransitionDetectorFactory = { automaton: Automaton ->
    FinalVerticesWithTransitionDetector(automaton)
}
val Automaton.finalVerticesWithTransitions
    get() = getModule(finalVerticesWithTransitionDetectorFactory).finalVerticesWithTransitions

class FinalVerticesWithTransitionDetector(val automaton: Automaton) : AutomatonModule {
    val finalVerticesWithTransitions = automaton.finalVertices.filteredSet {
        isNotEmpty(automaton.getOutgoingTransitions(it))
    }
}
