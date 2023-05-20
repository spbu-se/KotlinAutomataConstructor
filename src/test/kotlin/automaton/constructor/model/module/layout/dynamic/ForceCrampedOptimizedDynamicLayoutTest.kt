package automaton.constructor.model.module.layout.dynamic

import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.automaton.GRAPH_PANE_CENTER

class ForceCrampedOptimizedDynamicLayoutTest : ForceOptimizedDynamicLayoutTest() {
    override fun layout(automaton: Automaton) {
        automaton.vertices.forEach { it.position = GRAPH_PANE_CENTER }
        super.layout(automaton)
    }
}