package automaton.constructor.model.action

import automaton.constructor.model.action.ActionAvailability.AVAILABLE
import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.element.AutomatonElement
import automaton.constructor.utils.I18N
import javafx.scene.input.KeyCombination

interface AutomatonElementAction<in T : AutomatonElement> {
    val displayName: String

    val keyCombination: KeyCombination?

    fun getAvailabilityFor(actionSubject: T): ActionAvailability

    fun performOn(actionSubject: T)
}

abstract class AbstractAutomatonElementAction<A : Automaton, in T : AutomatonElement>(
    val automaton: A,
    override val displayName: String,
    override val keyCombination: KeyCombination? = null,
) : AutomatonElementAction<T> {
    protected abstract fun A.doGetAvailabilityFor(actionSubject: T): ActionAvailability
    protected abstract fun A.doPerformOn(actionSubject: T)

    override fun getAvailabilityFor(actionSubject: T) = automaton.doGetAvailabilityFor(actionSubject)
    override fun performOn(actionSubject: T) {
        if (getAvailabilityFor(actionSubject) != AVAILABLE)
            throw ActionFailedException(I18N.messages.getString("AutomatonElementAction.ActionNoLongerAvailable"))
        automaton.undoRedoManager.group {
            automaton.doPerformOn(actionSubject)
        }
    }
}
