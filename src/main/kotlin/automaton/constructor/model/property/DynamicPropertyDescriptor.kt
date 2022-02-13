package automaton.constructor.model.property

import automaton.constructor.model.Automaton
import javafx.scene.Node

/**
 * Describes a kind of [DynamicProperty]-s that all transitions or states of some [Automaton] must have
 * @see DynamicProperty
 */
class DynamicPropertyDescriptor<T>(
    val displayName: String,
    val defaultValue: T,
    /**
     * `true` properties described by this descriptor can be deemed epsilon if their value is [EPSILON_VALUE]
     *
     * It's preferable to set it to `false` if properties described by this descriptor can't have [EPSILON_VALUE]
     */
    val canBeDeemedEpsilon: Boolean,
    /**
     * Creates control for editing the `value` of the given property described by this descriptor
     * @see createEditor
     */
    private val editorFactory: (DynamicProperty<T>) -> Node,
    /**
     * Converts given value to a human-readable string
     * @see stringifyValue
     */
    private val stringifier: (T) -> String
) {
    /**
     * Creates [DynamicProperty] described by this descriptor with [defaultValue]
     */
    fun createProperty() = DynamicProperty(this)

    /**
     * Creates control for editing the `value` of the given [property] described by this descriptor
     */
    fun createEditor(property: DynamicProperty<T>): Node = editorFactory(property)

    /**
     * Converts given [value] to a human-readable string
     */
    fun stringifyValue(value: T): String = stringifier(value)
}
