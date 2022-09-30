package automaton.constructor.model.module

import automaton.constructor.model.automaton.Automaton
import automaton.constructor.utils.filteredSet

private val finalVerticesDetectorFactory = { automaton: Automaton -> FinalVerticesDetector(automaton) }
val Automaton.finalVertices get() = getModule(finalVerticesDetectorFactory).finalVertices

class FinalVerticesDetector(automaton: Automaton) : AutomatonModule {
    val finalVertices = automaton.vertices.filteredSet { it.isFinalProperty }
}
