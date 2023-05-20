package automaton.constructor.model.module.layout.dynamic

import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.automaton.GRAPH_PANE_CENTER

class ReCrampedDynamicLayoutTest : DynamicLayoutTest() {
    override fun layout(automaton: Automaton) {
        super.layout(automaton)
        automaton.vertices.forEach { it.position = GRAPH_PANE_CENTER }
        automaton.dynamicLayout.sync(DynamicLayoutPolicy.LAYOUT_NONE)
        super.layout(automaton)
    }
}