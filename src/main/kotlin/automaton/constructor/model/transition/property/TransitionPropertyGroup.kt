package automaton.constructor.model.transition.property

import automaton.constructor.model.memory.MemoryUnitDescriptor

data class TransitionPropertyGroup(
    val memoryUnitDescriptor: MemoryUnitDescriptor,
    val filterProperties: List<TransitionProperty<*>>,
    val sideEffectProperties: List<TransitionProperty<*>>
)
