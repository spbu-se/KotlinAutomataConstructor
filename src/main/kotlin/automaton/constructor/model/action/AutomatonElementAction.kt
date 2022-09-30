package automaton.constructor.model.action

import automaton.constructor.model.action.ActionAvailability.AVAILABLE
import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.element.AutomatonElement
import javafx.scene.input.KeyCombination

interface AutomatonElementAction<in T : AutomatonElement> {
    val displayName: String

    val keyCombination: KeyCombination?

    fun getAvailabilityFor(element: T): ActionAvailability

    fun performOn(element: T)
}

inline fun <A : Automaton, T : AutomatonElement> createAutomatonElementAction(
    automaton: A,
    displayName: String,
    keyCombination: KeyCombination? = null,
    crossinline getAvailabilityFor: A.(T) -> ActionAvailability,
    crossinline performOn: A.(T) -> Unit
) = object : AutomatonElementAction<T> {
    override val displayName = displayName

    override val keyCombination = keyCombination

    override fun getAvailabilityFor(element: T) = with(automaton) {
        undoRedoManager.group { getAvailabilityFor(element) }
    }

    override fun performOn(element: T) {
        check(getAvailabilityFor(element) == AVAILABLE) { "Requested action is not available" }
        with(automaton) { undoRedoManager.group { performOn(element) } }
    }
}
