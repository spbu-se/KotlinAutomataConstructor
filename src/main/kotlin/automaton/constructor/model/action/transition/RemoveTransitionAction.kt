package automaton.constructor.model.action.transition

import automaton.constructor.model.action.AbstractAutomatonElementAction
import automaton.constructor.model.action.ActionAvailability.AVAILABLE
import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.element.Transition
import automaton.constructor.utils.I18N
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination

class RemoveTransitionAction(automaton: Automaton) : AbstractAutomatonElementAction<Automaton, Transition>(
    automaton = automaton,
    displayName = I18N.messages.getString("AutomatonElementAction.RemoveTransition"),
    keyCombination = KeyCodeCombination(KeyCode.DELETE)
) {
    override fun Automaton.doGetAvailabilityFor(actionSubject: Transition) = AVAILABLE

    override fun Automaton.doPerformOn(actionSubject: Transition) = removeTransition(actionSubject)
}
