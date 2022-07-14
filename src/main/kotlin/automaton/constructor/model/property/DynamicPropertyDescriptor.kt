package automaton.constructor.model.property

import automaton.constructor.model.automaton.Automaton
import javafx.scene.Node
import javafx.util.StringConverter

/**
 * Describes a kind of [DynamicProperty]-s that all transitions or states of some [Automaton] must have
 * @see DynamicProperty
 */
class DynamicPropertyDescriptor<T>(
    /**
     * Displayable name of this descriptor
     */
    val displayName: String,
    /**
     * The default value for a dynamic property [created][createProperty] by this descriptor
     */
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
     * Converts a given value to a human-readable string.
     * This string can be later deserialized back to the given value
     */
    val stringConverter: StringConverter<T>,
    /**
     * Converts a given value to a human-readable string **intended only for display**.
     * There are no guarantees this string can be later deserialized back to the given value
     */
    val displayValueFactory: (T) -> String = stringConverter::toString
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
     * Creates a copy of this descriptor.
     */
    fun copy(
        displayName: String = this.displayName,
        defaultValue: T = this.defaultValue,
        canBeDeemedEpsilon: Boolean = this.canBeDeemedEpsilon,
        editorFactory: (DynamicProperty<T>) -> Node = this.editorFactory,
        stringConverter: StringConverter<T> = this.stringConverter,
        displayValueFactory: (T) -> String = this.displayValueFactory,
    ) = DynamicPropertyDescriptor(
        displayName,
        defaultValue,
        canBeDeemedEpsilon,
        editorFactory,
        stringConverter,
        displayValueFactory
    )
}
