package automaton.constructor.model.transition.property

import javafx.beans.property.Property
import tornadofx.*

// If transition filter value is EPSILON_VALUE then the filter is considered to always be satisfied.
//
// If all transition properties have EPSILON_VALUE (including both filters and sideEffects) then the transition
// is always possible regardless of memory current state and has no effect on the memory (a.k.a. pure transition).
// Executor may take pure transitions with asking nor informing memory units (used for step by closure strategy).
val EPSILON_VALUE = null

class TransitionProperty<T>(
    delegate: Property<T>,
    val descriptor: TransitionPropertyDescriptor<T>,
) : Property<T> by delegate {
    val isEpsilonProperty =
        if (descriptor.canBeDeemedEpsilon) booleanBinding { value == EPSILON_VALUE }
        else false.toProperty()
    val isEpsilon by isEpsilonProperty
    val stringValue: String get() = descriptor.stringifyValue(value)
    override fun getName() = descriptor.name

    // TODO maybe return some sealed interface implementation that is mapped to control by view
    fun createSettingControl() = descriptor.createSettingControl(this)
}
