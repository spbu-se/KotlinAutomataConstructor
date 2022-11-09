package automaton.constructor.model.action.state

import automaton.constructor.model.action.AbstractAction
import automaton.constructor.model.action.ActionAvailability.AVAILABLE
import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.element.State
import automaton.constructor.utils.I18N
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination

class RemoveStateAction(automaton: Automaton) : AbstractAction<Automaton, State>(
    automaton = automaton,
    displayName = I18N.messages.getString("Action.RemoveState"),
    keyCombination = KeyCodeCombination(KeyCode.DELETE)
) {
    override fun Automaton.doGetAvailabilityFor(actionSubject: State) = AVAILABLE

    override fun Automaton.doPerformOn(actionSubject: State) = removeVertex(actionSubject)
}
