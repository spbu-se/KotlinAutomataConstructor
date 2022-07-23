package automaton.constructor.model.action

import automaton.constructor.model.State
import automaton.constructor.model.automaton.MealyMooreMachine
import automaton.constructor.model.transition.Transition
import automaton.constructor.utils.I18N
import automaton.constructor.view.StateView.Companion.RADIUS
import tornadofx.*

class MealyToMooreAction(private val automaton: MealyMooreMachine) : AutomatonElementAction<State> {
    override val displayName: String = I18N.messages.getString("AutomatonElementAction.MealyToMoore")

    override fun isAvailableFor(element: State): ActionAvailability {
        val isAvailable = automaton.getTransitionsTo(element)
            .flatMap { it.transitionSideEffects }
            .mapNotNull { it.value }
            .isNotEmpty()

        return if (isAvailable) ActionAvailability.AVAILABLE else ActionAvailability.DISABLED
    }

    override fun performOn(element: State) {
        data class MealyMooreTransitionData(
            val source: State,
            var target: State,
            val filter: Any?,
            val output: Any?
        )

        fun State.output() = sideEffects.first()
        fun Transition.filter() = transitionFilters.first()
        fun Transition.output() = transitionSideEffects.first()


        automaton.undoRedoManager.group {
            val mooreOutputProp = element.output()
            val mooreOutput = mooreOutputProp.value ?: ""

            val transitions = (automaton.getTransitionsTo(element) + automaton.getTransitionsFrom(element)).toList()

            val transitionsData = transitions.map {
                MealyMooreTransitionData(
                    source = it.source,
                    target = it.target,
                    filter = it.filter().value,
                    output = it.output().value
                )
            }

            val (loops, nonLoops) = transitionsData.partition { it.source == it.target }
            val (strictlyIncomingTransitions, strictlyOutgoingTransitions) = nonLoops.partition { it.target == element }
            val incomingTransitions = transitionsData.filter { it.target == element }
            val incomingTransitionsGroups = incomingTransitions.groupBy(MealyMooreTransitionData::output)


            val mooreStatesWithTransitions =
                incomingTransitionsGroups.toList().mapIndexed { i, (output, transitionsToMooreState) ->
                    val mooreState = automaton.addState(position = element.position + i * 4 * RADIUS).apply {
                        output().value = "${output ?: ""}$mooreOutput"
                    }
                    mooreState to transitionsToMooreState
                }

            for ((mooreState, transitionsToMooreState) in mooreStatesWithTransitions) {
                for (transition in transitionsToMooreState) {
                    transition.target = mooreState
                }
            }

            val mooreStates = mooreStatesWithTransitions.map { it.first }

            for ((source, target, filter) in strictlyIncomingTransitions) {
                automaton.addTransition(source, target).apply {
                    filter().value = filter
                }
            }
            for (mooreState in mooreStates) {
                for ((_, target, filter, output) in strictlyOutgoingTransitions) {
                    automaton.addTransition(mooreState, target).apply {
                        filter().value = filter
                        output().value = output
                    }
                }
                for ((_, target, filter) in loops) {
                    automaton.addTransition(mooreState, target).apply {
                        filter().value = filter
                    }
                }
            }

            automaton.removeState(element)
        }
    }
}
