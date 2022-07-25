package automaton.constructor.model.action.element

import automaton.constructor.model.action.ActionAvailability.AVAILABLE
import automaton.constructor.model.action.createAutomatonElementAction
import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.transition.Transition
import automaton.constructor.utils.I18N
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination

fun createRemoveTransitionAction(automaton: Automaton) = createAutomatonElementAction<Automaton, Transition>(
    automaton = automaton,
    displayName = I18N.messages.getString("AutomatonElementAction.RemoveTransition"),
    keyCombination = KeyCodeCombination(KeyCode.DELETE),
    getAvailabilityFor = { AVAILABLE },
    performOn = { removeTransition(it) }
)
