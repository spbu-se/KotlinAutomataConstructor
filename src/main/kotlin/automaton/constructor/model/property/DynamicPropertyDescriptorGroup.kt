package automaton.constructor.model.property

import automaton.constructor.model.memory.MemoryUnitDescriptor

data class DynamicPropertyDescriptorGroup(
    val memoryUnitDescriptor: MemoryUnitDescriptor,
    val filters: List<DynamicPropertyDescriptor<*>>,
    val sideEffects: List<DynamicPropertyDescriptor<*>>
)
