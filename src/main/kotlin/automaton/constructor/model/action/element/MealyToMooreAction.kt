package automaton.constructor.model.action.element

import automaton.constructor.model.State
import automaton.constructor.model.State.Companion.RADIUS
import automaton.constructor.model.action.ActionAvailability.AVAILABLE
import automaton.constructor.model.action.ActionAvailability.DISABLED
import automaton.constructor.model.action.createAutomatonElementAction
import automaton.constructor.model.automaton.MealyMooreMachine
import automaton.constructor.model.automaton.copyAndAddState
import automaton.constructor.model.automaton.copyAndAddTransition
import automaton.constructor.model.automaton.getOutgoingTransitionsWithoutLoops
import automaton.constructor.model.property.EPSILON_VALUE
import automaton.constructor.utils.I18N
import automaton.constructor.utils.partitionToSets
import tornadofx.*

fun createMealyToMooreElementAction(mealyMooreMachine: MealyMooreMachine) =
    createAutomatonElementAction<MealyMooreMachine, State>(
        automaton = mealyMooreMachine,
        displayName = I18N.messages.getString("AutomatonElementAction.MealyToMoore"),
        getAvailabilityFor = { state ->
            if (getIncomingTransitions(state).any { it.outputValue != EPSILON_VALUE }) {
                AVAILABLE
            } else {
                DISABLED
            }
        },
        performOn = { state ->
            val incomingTransitions = getIncomingTransitions(state)
            val (incomingTransitionsWithoutLoops, loops) = getIncomingTransitions(state).partitionToSets { !it.isLoop() }
            val outgoingTransitionsWithoutLoops = getOutgoingTransitionsWithoutLoops(state)

            val stateOutputString = state.notNullOutputValue

            val incomingTransitionsGroups = incomingTransitions.groupBy { it.notNullOutputValue }

            val mooreOutputStringsToMooreStates = incomingTransitionsGroups.keys.mapIndexed { i, incomingOutputString ->
                val mooreState = copyAndAddState(
                    state,
                    newPosition = state.position + i * 4 * RADIUS,
                    newIsInitial = state.isInitial && i == 0
                ).apply {
                    outputValue = "$incomingOutputString$stateOutputString"
                }
                incomingOutputString to mooreState
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

            removeState(state)
        }
    )
