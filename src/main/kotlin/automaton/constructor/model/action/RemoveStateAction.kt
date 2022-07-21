package automaton.constructor.model.action

import automaton.constructor.model.State
import automaton.constructor.model.automaton.Automaton
import automaton.constructor.utils.I18N
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination

class RemoveStateAction(private val automaton: Automaton) : AutomatonElementAction<State> {
    override val displayName: String = I18N.messages.getString("AutomatonElementAction.RemoveState")

    override val keyCombination = KeyCodeCombination(KeyCode.DELETE)

    override fun isAvailableFor(element: State) = ActionAvailability.AVAILABLE

    override fun performOn(element: State) {
        automaton.removeState(element)
    }
}
