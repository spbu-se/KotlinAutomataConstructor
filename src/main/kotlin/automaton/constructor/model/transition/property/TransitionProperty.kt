package automaton.constructor.model.transition.property

import automaton.constructor.utils.Editable
import javafx.beans.property.SimpleObjectProperty

// If transition filter value is EPSILON_VALUE then the filter is considered to always be satisfied.
//
// If all transition properties have EPSILON_VALUE (including both filters and sideEffects) then the transition
// is always possible regardless of memory current state and has no effect on the memory (a.k.a. pure transition).
// Executor may take pure transitions with asking nor informing memory units (used for step by closure strategy).
val EPSILON_VALUE = null

class TransitionProperty<T>(
    val descriptor: TransitionPropertyDescriptor<T>
) : SimpleObjectProperty<T>(descriptor.defaultValue), Editable {
    fun stringify(): String = descriptor.stringifyValue(value)
    override val displayName get() = descriptor.displayName

    // TODO maybe return some sealed interface implementation that is mapped to control by view
    override fun createEditor() = descriptor.createEditor(this)
}
