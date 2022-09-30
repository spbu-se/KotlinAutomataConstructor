package automaton.constructor.model.property

data class DynamicPropertyDescriptorGroup(
    val displayName: String,
    val filters: List<DynamicPropertyDescriptor<*>>,
    val sideEffects: List<DynamicPropertyDescriptor<*>>
)
