package automaton.constructor.model.property

import automaton.constructor.model.memory.MemoryUnitDescriptor

/**
 * A group of automaton element properties that have been created by property descriptors of the [memoryUnitDescriptor]
 */
data class DynamicPropertyGroup(
    val memoryUnitDescriptor: MemoryUnitDescriptor,
    val filters: List<DynamicProperty<*>>,
    val sideEffects: List<DynamicProperty<*>>
)
