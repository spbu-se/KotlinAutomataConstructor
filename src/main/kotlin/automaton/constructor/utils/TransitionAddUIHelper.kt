package automaton.constructor.utils

import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.element.AutomatonVertex
import automaton.constructor.model.element.Transition
import javafx.stage.Window
import tornadofx.information

/**
 * Tries to add a transition and shows an information dialog if it is not allowed.
 *
 * @return the created Transition on success, or null on failure (after showing a message).
 */
fun tryAddTransitionWithDialog(
    automaton: Automaton,
    source: AutomatonVertex,
    target: AutomatonVertex,
    owner: Window?
): Transition? {
    return when (val result = automaton.safeAddTransition(source, target)) {
        is Automaton.AddTransitionResult.Success -> result.transition
        is Automaton.AddTransitionResult.Failure -> {
            information(
                result.message.ifBlank { I18N.messages.getString("Dialog.error.TransitionNotAllowed") },
                title = I18N.messages.getString("Dialog.information"),
                owner = owner
            )
            null
        }
    }
}