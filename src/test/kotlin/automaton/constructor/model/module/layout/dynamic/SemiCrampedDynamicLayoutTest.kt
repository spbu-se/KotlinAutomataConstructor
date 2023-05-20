package automaton.constructor.model.module.layout.dynamic

import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.automaton.GRAPH_PANE_CENTER

class SemiCrampedDynamicLayoutTest : DynamicLayoutTest() {
    override fun layout(automaton: Automaton) {
        automaton.vertices.forEachIndexed { i, vertex ->
            if (i % 2 == 0)
                vertex.position = GRAPH_PANE_CENTER
        }
        super.layout(automaton)
    }
}