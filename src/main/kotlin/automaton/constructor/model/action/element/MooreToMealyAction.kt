package automaton.constructor.model.action.element

import automaton.constructor.model.State
import automaton.constructor.model.action.ActionAvailability.AVAILABLE
import automaton.constructor.model.action.ActionAvailability.DISABLED
import automaton.constructor.model.action.createAutomatonElementAction
import automaton.constructor.model.automaton.MealyMooreMachine
import automaton.constructor.model.property.EPSILON_VALUE
import automaton.constructor.utils.I18N

fun createMooreToMealyElementAction(mealyMooreMachine: MealyMooreMachine) =
    createAutomatonElementAction<MealyMooreMachine, State>(
        automaton = mealyMooreMachine,
        displayName = I18N.messages.getString("AutomatonElementAction.MooreToMealy"),
        getAvailabilityFor = { state ->
            if (state.mealyMooreOutputValue != EPSILON_VALUE) AVAILABLE else DISABLED
        },
        performOn = { state ->
            val mooreOutputString = state.mealyMooreOutputValue
            state.mealyMooreOutputValue = EPSILON_VALUE
            for (incomingTransition in getIncomingTransitions(state)) {
                val str = incomingTransition.mealyMooreNotNullOutputValue
                incomingTransition.mealyMooreOutputValue = "$str$mooreOutputString"
            }
        }
    )
