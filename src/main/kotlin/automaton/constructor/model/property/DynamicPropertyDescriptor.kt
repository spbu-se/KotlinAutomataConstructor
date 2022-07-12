package automaton.constructor.model.property

import automaton.constructor.model.automaton.Automaton
import javafx.scene.Node
import javafx.util.StringConverter

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
     */
    val stringConverter: StringConverter<T>
) {
    /**
     * Creates [DynamicProperty] described by this descriptor with [defaultValue]
     */
    fun createProperty() = DynamicProperty(this)

    /**
     * Creates control for editing the `value` of the given [property] described by this descriptor
     */
    fun createEditor(property: DynamicProperty<T>): Node = editorFactory(property)
}
