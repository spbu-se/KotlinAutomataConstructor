package automaton.constructor.model.transition.property

import automaton.constructor.model.memory.MemoryUnitDescriptor

data class TransitionPropertyGroup(
    val memoryUnitDescriptor: MemoryUnitDescriptor,
    val filters: List<TransitionProperty<*>>,
    val sideEffects: List<TransitionProperty<*>>
)
