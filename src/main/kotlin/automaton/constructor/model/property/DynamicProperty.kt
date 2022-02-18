package automaton.constructor.model.property

import automaton.constructor.model.transition.Transition
import automaton.constructor.utils.Editable
import javafx.beans.property.SimpleObjectProperty

/**
 * If transition filter value is [EPSILON_VALUE] then the filter is considered to always be satisfied
 * @see Transition.isPure
 */
val EPSILON_VALUE = null

/**
 * A property of a [Transition] that is either a filter or a side effect
 */
class DynamicProperty<T>(
    val descriptor: DynamicPropertyDescriptor<T>
) : SimpleObjectProperty<T>(descriptor.defaultValue), Editable {
    /**
     * @see DynamicPropertyDescriptor.displayName
     */
    override val displayName get() = descriptor.displayName

    override fun createEditor() = descriptor.createEditor(this)

    override fun toString(): String = descriptor.stringConverter.toString(value)
}
