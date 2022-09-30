package automaton.constructor.model.property

/**
 * A named group of automaton element properties
 */
data class DynamicPropertyGroup(
    val displayName: String,
    val filters: List<DynamicProperty<*>>,
    val sideEffects: List<DynamicProperty<*>>
)
