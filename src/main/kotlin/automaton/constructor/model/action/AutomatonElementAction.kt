package automaton.constructor.model.action

import automaton.constructor.model.property.AutomatonElement
import javafx.scene.input.KeyCombination

interface AutomatonElementAction<in T : AutomatonElement> {
    val displayName: String

    val keyCombination: KeyCombination? get() = null

    fun isAvailableFor(element: T): ActionAvailability

    fun performOn(element: T)
}
