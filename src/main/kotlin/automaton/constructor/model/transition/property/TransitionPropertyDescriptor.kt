package automaton.constructor.model.transition.property

import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.Control

class TransitionPropertyDescriptor<T>(
    val name: String,
    val defaultValue: T,
    // only influences "with epsilon transitions"/"without epsilon transitions" message shown to a user
    // (e.g. it doesn't make much sense to deem transition epsilon just because it writes an empty string (ε) to a stack)
    val canBeDeemedEpsilon: Boolean,
    private val settingControlFactory: (TransitionProperty<T>) -> Control,
    private val stringifier: (T) -> String
) {
    fun createProperty() = TransitionProperty(SimpleObjectProperty(defaultValue), this)

    fun createSettingControl(property: TransitionProperty<T>): Control = settingControlFactory(property)

    fun stringifyValue(value: T): String = stringifier(value)
}
