package automaton.constructor.model.action.state

import automaton.constructor.model.action.AbstractAutomatonElementAction
import automaton.constructor.model.action.ActionAvailability.AVAILABLE
import automaton.constructor.model.action.ActionAvailability.DISABLED
import automaton.constructor.model.automaton.MealyMooreMachine
import automaton.constructor.model.element.AutomatonVertex.Companion.RADIUS
import automaton.constructor.model.element.State
import automaton.constructor.model.property.EPSILON_VALUE
import automaton.constructor.utils.I18N
import javafx.geometry.Point2D
import tornadofx.*

class MooreToMealyAction(mealyMooreMachine: MealyMooreMachine) :
    AbstractAutomatonElementAction<MealyMooreMachine, State>(
        automaton = mealyMooreMachine,
        displayName = I18N.messages.getString("AutomatonElementAction.MooreToMealy")
    ) {
    override fun MealyMooreMachine.doGetAvailabilityFor(actionSubject: State) =
        if (actionSubject.outputValue != EPSILON_VALUE) AVAILABLE else DISABLED

    override fun MealyMooreMachine.doPerformOn(actionSubject: State) {
        val mooreOutputString = actionSubject.outputValue
        actionSubject.outputValue = EPSILON_VALUE
        for (incomingTransition in getIncomingTransitions(actionSubject)) {
            val str = incomingTransition.notNullOutputValue
            incomingTransition.outputValue = "$str$mooreOutputString"
        }
        if (actionSubject.isInitial) {
            val initState = addState(
                "init ${actionSubject.name}",
                actionSubject.position - Point2D(RADIUS * 6, 0.0)
            )
            actionSubject.isInitial = false
            initState.isInitial = true
            addTransition(initState, actionSubject).outputValue = mooreOutputString
        }
    }
}
