package automaton.constructor.model.transition.property

import automaton.constructor.model.memory.MemoryUnitDescriptor

/**
 * A group of transition properties that have been created by property descriptors of the [memoryUnitDescriptor]
 */
data class TransitionPropertyGroup(
    val memoryUnitDescriptor: MemoryUnitDescriptor,
    val filters: List<TransitionProperty<*>>,
    val sideEffects: List<TransitionProperty<*>>
)
