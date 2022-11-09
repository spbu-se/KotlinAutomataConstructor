package automaton.constructor.model.action

import automaton.constructor.model.action.ActionAvailability.AVAILABLE
import automaton.constructor.model.automaton.Automaton
import automaton.constructor.utils.I18N
import javafx.scene.input.KeyCombination

interface Action<in T> {
    val displayName: String

    val keyCombination: KeyCombination?

    fun getAvailabilityFor(actionSubject: T): ActionAvailability

    fun performOn(actionSubject: T)
}

fun Action<Unit>.perform() = performOn(Unit)

abstract class AbstractAction<A : Automaton, in T>(
    val automaton: A,
    override val displayName: String,
    override val keyCombination: KeyCombination? = null,
) : Action<T> {
    protected abstract fun A.doGetAvailabilityFor(actionSubject: T): ActionAvailability
    protected abstract fun A.doPerformOn(actionSubject: T)

    override fun getAvailabilityFor(actionSubject: T) = automaton.doGetAvailabilityFor(actionSubject)
    override fun performOn(actionSubject: T) {
        if (getAvailabilityFor(actionSubject) != AVAILABLE)
            throw ActionFailedException(I18N.messages.getString("Action.ActionNoLongerAvailable"))
        automaton.undoRedoManager.group {
            automaton.doPerformOn(actionSubject)
        }
    }
}
