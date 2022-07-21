package automaton.constructor.model.action

import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.transition.Transition
import automaton.constructor.utils.I18N
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination

class RemoveTransitionAction(private val automaton: Automaton) : AutomatonElementAction<Transition> {
    override val displayName: String = I18N.messages.getString("AutomatonElementAction.RemoveTransition")

    override val keyCombination = KeyCodeCombination(KeyCode.DELETE)

    override fun isAvailableFor(element: Transition) = ActionAvailability.AVAILABLE

    override fun performOn(element: Transition) {
        automaton.removeTransition(element)
    }
}
