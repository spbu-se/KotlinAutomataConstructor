package automaton.constructor.model.action.element

import automaton.constructor.model.State
import automaton.constructor.model.action.ActionAvailability.AVAILABLE
import automaton.constructor.model.action.createAutomatonElementAction
import automaton.constructor.model.automaton.Automaton
import automaton.constructor.utils.I18N
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination

fun createRemoveStateAction(automaton: Automaton) = createAutomatonElementAction<Automaton, State>(
    automaton = automaton,
    displayName = I18N.messages.getString("AutomatonElementAction.RemoveState"),
    keyCombination = KeyCodeCombination(KeyCode.DELETE),
    getAvailabilityFor = { AVAILABLE },
    performOn = { removeState(it) }
)
