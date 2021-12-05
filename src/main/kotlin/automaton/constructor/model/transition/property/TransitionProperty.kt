package automaton.constructor.model.transition.property

import automaton.constructor.utils.Editable
import automaton.constructor.model.transition.Transition
import javafx.beans.property.SimpleObjectProperty

/**
 * If transition filter value is [EPSILON_VALUE] then the filter is considered to always be satisfied
 * @see Transition.isPure
 */
val EPSILON_VALUE = null

/**
 * A property of a [Transition] that is either a filter or a side effect
 */
class TransitionProperty<T>(
    val descriptor: TransitionPropertyDescriptor<T>
) : SimpleObjectProperty<T>(descriptor.defaultValue), Editable {
    /**
     * Converts current [value] of the property to a human-readable string
     * @see TransitionPropertyDescriptor.stringifyValue
     */
    fun stringify(): String = descriptor.stringifyValue(value)

    /**
     * @see TransitionPropertyDescriptor.displayName
     */
    override val displayName get() = descriptor.displayName

    override fun createEditor() = descriptor.createEditor(this)
}
