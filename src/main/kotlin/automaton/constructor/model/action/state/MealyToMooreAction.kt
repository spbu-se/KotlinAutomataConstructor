package automaton.constructor.model.action.state

import automaton.constructor.model.action.AbstractAction
import automaton.constructor.model.action.ActionAvailability.AVAILABLE
import automaton.constructor.model.action.ActionAvailability.DISABLED
import automaton.constructor.model.automaton.MealyMooreMachine
import automaton.constructor.model.automaton.copyAndAddState
import automaton.constructor.model.automaton.copyAndAddTransition
import automaton.constructor.model.automaton.getOutgoingTransitionsWithoutLoops
import automaton.constructor.model.element.AutomatonVertex.Companion.RADIUS
import automaton.constructor.model.element.State
import automaton.constructor.model.property.EPSILON_VALUE
import automaton.constructor.utils.I18N
import automaton.constructor.utils.partitionToSets
import tornadofx.*

class MealyToMooreAction(mealyMooreMachine: MealyMooreMachine) :
    AbstractAction<MealyMooreMachine, State>(
        automaton = mealyMooreMachine,
        displayName = I18N.messages.getString("Action.MealyToMoore")
    ) {
    override fun MealyMooreMachine.doGetAvailabilityFor(actionSubject: State) =
        if (getIncomingTransitions(actionSubject).any { it.outputValue != EPSILON_VALUE }) {
            AVAILABLE
        } else {
            DISABLED
        }

    override fun MealyMooreMachine.doPerformOn(actionSubject: State) {
        val incomingTransitions = getIncomingTransitions(actionSubject)
        val (incomingTransitionsWithoutLoops, loops) = getIncomingTransitions(actionSubject).partitionToSets { !it.isLoop() }
        val outgoingTransitionsWithoutLoops = getOutgoingTransitionsWithoutLoops(actionSubject)

        val stateOutput = actionSubject.outputValue

        val incomingTransitionsGroups = incomingTransitions.groupByTo(mutableMapOf()) { it.outputValue }

        if (actionSubject.isInitial && EPSILON_VALUE !in incomingTransitionsGroups)
            incomingTransitionsGroups[EPSILON_VALUE] = mutableListOf()

        var i = if (EPSILON_VALUE in incomingTransitionsGroups) 1 else 0
        val mooreOutputStringsToMooreStates = incomingTransitionsGroups.keys.map { transitionOutput ->
            val mooreState = copyAndAddState(
                actionSubject,
                newPosition = actionSubject.position + (if (transitionOutput == EPSILON_VALUE) 0 else i++) * 4 * RADIUS,
                newIsInitial = actionSubject.isInitial && transitionOutput == EPSILON_VALUE
            ).apply {
                outputValue = ((transitionOutput.takeIf { it != EPSILON_VALUE } ?: "") +
                        (stateOutput.takeIf { it != EPSILON_VALUE } ?: "")).ifEmpty { EPSILON_VALUE }
                requiresLayout = incomingTransitionsGroups.size > 1
            }
            transitionOutput to mooreState
        }
        val mooreStates = mooreOutputStringsToMooreStates.map { it.second }
        val incomingTransitionsToTargets =
            mooreOutputStringsToMooreStates.flatMap { (mooreOutputString, mooreState) ->
                incomingTransitionsGroups.getValue(mooreOutputString).map { it to mooreState }
            }.toMap()

        for (t in incomingTransitionsWithoutLoops) {
            copyAndAddTransition(t, newTarget = incomingTransitionsToTargets.getValue(t)).apply {
                outputValue = EPSILON_VALUE
            }
        }
        for (mooreState in mooreStates) {
            for (t in loops) {
                copyAndAddTransition(
                    t,
                    newSource = mooreState,
                    newTarget = incomingTransitionsToTargets.getValue(t)
                ).apply {
                    outputValue = EPSILON_VALUE
                }
            }
            for (t in outgoingTransitionsWithoutLoops) {
                copyAndAddTransition(t, newSource = mooreState)
            }
        }

        removeVertex(actionSubject)
    }

}
