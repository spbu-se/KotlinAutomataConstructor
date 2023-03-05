package automaton.constructor.model.module.layout.dynamic

import automaton.constructor.model.module.AutomatonModule

interface DynamicLayout : AutomatonModule {
    fun sync(policy: DynamicLayoutPolicy)
    fun step(policy: DynamicLayoutPolicy)
}
