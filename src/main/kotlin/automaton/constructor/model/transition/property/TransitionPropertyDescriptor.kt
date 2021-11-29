package automaton.constructor.model.transition.property

import javafx.scene.Node

class TransitionPropertyDescriptor<T>(
    val displayName: String,
    val defaultValue: T,
    // only influences "with epsilon transitions"/"without epsilon transitions" message shown to a user
    // (e.g. it doesn't make much sense to deem transition epsilon just because it writes an empty string (ε) to a stack)
    val canBeDeemedEpsilon: Boolean,
    private val editorFactory: (TransitionProperty<T>) -> Node,
    private val stringifier: (T) -> String
) {
    fun createProperty() = TransitionProperty(this)

    fun createEditor(property: TransitionProperty<T>): Node = editorFactory(property)

    fun stringifyValue(value: T): String = stringifier(value)
}
