package automaton.constructor.model.action.state

import automaton.constructor.model.action.ActionAvailability.AVAILABLE
import automaton.constructor.model.action.ActionAvailability.DISABLED
import automaton.constructor.model.action.createAutomatonElementAction
import automaton.constructor.model.automaton.MealyMooreMachine
import automaton.constructor.model.element.AutomatonVertex.Companion.RADIUS
import automaton.constructor.model.element.State
import automaton.constructor.model.property.EPSILON_VALUE
import automaton.constructor.utils.I18N
import javafx.geometry.Point2D
import tornadofx.*

fun createMooreToMealyElementAction(mealyMooreMachine: MealyMooreMachine) =
    createAutomatonElementAction<MealyMooreMachine, State>(
        automaton = mealyMooreMachine,
        displayName = I18N.messages.getString("AutomatonElementAction.MooreToMealy"),
        getAvailabilityFor = { state ->
            if (state.outputValue != EPSILON_VALUE) AVAILABLE else DISABLED
        },
        performOn = { state ->
            val mooreOutputString = state.outputValue
            state.outputValue = EPSILON_VALUE
            for (incomingTransition in getIncomingTransitions(state)) {
                val str = incomingTransition.notNullOutputValue
                incomingTransition.outputValue = "$str$mooreOutputString"
            }
            if (state.isInitial) {
                val initState = mealyMooreMachine.addState(
                    "init ${state.name}",
                    state.position - Point2D(RADIUS * 6, 0.0)
                )
                state.isInitial = false
                initState.isInitial = true
                mealyMooreMachine.addTransition(initState, state).outputValue = mooreOutputString
            }
        }
    )
