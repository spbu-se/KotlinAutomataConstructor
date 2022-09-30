package automaton.constructor.model.module

import automaton.constructor.model.automaton.Automaton
import automaton.constructor.utils.filteredSet

private val initialVerticesDetectorFactory = { automaton: Automaton -> InitialVerticesDetector(automaton) }
val Automaton.initialVertices get() = getModule(initialVerticesDetectorFactory).initialVertices

class InitialVerticesDetector(automaton: Automaton) : AutomatonModule {
    val initialVertices = automaton.vertices.filteredSet { it.isInitialProperty }
}
