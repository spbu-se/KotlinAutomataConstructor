package automaton.constructor.model.action

import automaton.constructor.model.State
import automaton.constructor.model.automaton.MealyMooreMachine
import automaton.constructor.model.property.EPSILON_VALUE
import automaton.constructor.utils.I18N

class MooreToMealyAction(private val automaton: MealyMooreMachine) : AutomatonElementAction<State> {
    override val displayName: String = I18N.messages.getString("AutomatonElementAction.MooreToMealy")

    override fun isAvailableFor(element: State) =
        if (element.sideEffects.first().value != EPSILON_VALUE) {
            ActionAvailability.AVAILABLE
        } else {
            ActionAvailability.DISABLED
        }

    override fun performOn(element: State) {
        automaton.undoRedoManager.group {
            val mooreOutputProp = element.sideEffects.first()
            val mooreOutput = mooreOutputProp.value

            mooreOutputProp.value = EPSILON_VALUE

            automaton.getTransitionsTo(element)
                .flatMap { it.transitionSideEffects }
                .forEach { it.value = "${it.value ?: ""}$mooreOutput" }
        }
    }
}
