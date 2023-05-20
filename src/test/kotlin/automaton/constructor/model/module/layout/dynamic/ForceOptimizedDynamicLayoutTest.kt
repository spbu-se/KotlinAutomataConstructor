package automaton.constructor.model.module.layout.dynamic

import automaton.constructor.model.automaton.Automaton
import org.junit.jupiter.api.Assumptions.assumeTrue

open class ForceOptimizedDynamicLayoutTest : DynamicLayoutTest() {
    override fun layout(automaton: Automaton) {
        val fa2 = automaton.dynamicLayout as? ForceAtlas2Layout
        assumeTrue(fa2 != null)
        fa2!!.forceOptimized = true
        super.layout(automaton)
    }
}