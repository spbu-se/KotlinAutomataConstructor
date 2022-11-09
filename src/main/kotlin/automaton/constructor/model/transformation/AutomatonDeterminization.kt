package automaton.constructor.model.transformation

import automaton.constructor.model.action.AbstractAction
import automaton.constructor.model.action.ActionAvailability
import automaton.constructor.model.action.ActionFailedException
import automaton.constructor.model.automaton.FiniteAutomaton
import automaton.constructor.model.element.AutomatonElement
import automaton.constructor.model.element.AutomatonVertex
import automaton.constructor.model.element.State
import automaton.constructor.model.module.hasEpsilon
import automaton.constructor.model.module.initialVertices
import automaton.constructor.model.module.isDeterministic
import automaton.constructor.utils.I18N
import automaton.constructor.view.GRAPH_PANE_CENTER
import javafx.beans.binding.Bindings.isEmpty
import javafx.collections.SetChangeListener
import javafx.geometry.Point2D
import javafx.scene.transform.Rotate
import tornadofx.*
import java.text.MessageFormat

class AutomatonDeterminization(
    private val nfa: FiniteAutomaton,
    private val dfa: FiniteAutomaton = FiniteAutomaton(nfa.inputTape)
) : AbstractAutomatonTransformation(nfa, dfa) {
    override val displayName: String = I18N.messages.getString("AutomatonDeterminization.Determinization")
    override val description: String = I18N.messages.getString("AutomatonDeterminization.StepByStepInstruction")

    private val statesToCombinedStateMap = mutableMapOf<Set<AutomatonVertex>, State>()
    private val combinedStateToStatesMap = mutableMapOf<State, Set<AutomatonVertex>>()
    private val unexpandedStates = observableSetOf<State>().apply {
        addListener(SetChangeListener {
            if (it.wasAdded()) it.elementAdded.isHighlighted = true
            if (it.wasRemoved()) it.elementRemoved.isHighlighted = false
        })
    }

    init {
        getOrCreateCombinedState(nfa.initialVertices).isInitial = true
        isCompletedProperty.bind(isEmpty(unexpandedStates))
    }

    override fun step(stepSubject: AutomatonElement) {
        if (stepSubject is State && stepSubject in unexpandedStates) {
            var nextTransitionAngleDegrees = 45.0
            combinedStateToStatesMap.getValue(stepSubject)
                .flatMap { nfa.getOutgoingTransitions(it) }
                .groupBy(keySelector = { it[nfa.inputTape.expectedChar] }, valueTransform = { it.target })
                .forEach { (char, targets) ->
                    dfa.addTransition(
                        stepSubject,
                        getOrCreateCombinedState(targets.toSet()) {
                            stepSubject.position +
                                    Rotate(nextTransitionAngleDegrees.also { nextTransitionAngleDegrees += 48.0 })
                                        .transform(Vector2D(10 * AutomatonVertex.RADIUS, 0.0))
                        }
                    )[dfa.inputTape.expectedChar] = char
                }
            unexpandedStates.remove(stepSubject)
        }
    }

    override fun complete() {
        while (unexpandedStates.isNotEmpty())
            step(unexpandedStates.first())
    }

    private inline fun getOrCreateCombinedState(
        vertices: Set<AutomatonVertex>,
        positionProvider: () -> Point2D = { GRAPH_PANE_CENTER }
    ) = statesToCombinedStateMap.getOrPut(vertices) {
        dfa.addState(
            name = vertices.joinToString(separator = ",") { it.name },
            position = positionProvider()
        ).also { combinedState ->
            combinedState.isFinal = vertices.any { it.isFinal }
            combinedStateToStatesMap[combinedState] = vertices
            if (vertices.any { nfa.getOutgoingTransitions(it).isNotEmpty() })
                unexpandedStates.add(combinedState)
        }
    }
}

class DeterminizeAutomatonAction(
    automaton: FiniteAutomaton
) : AbstractAction<FiniteAutomaton, Unit>(automaton, I18N.messages.getString("AutomatonDeterminization.Determinize")) {
    override fun FiniteAutomaton.doGetAvailabilityFor(actionSubject: Unit) = ActionAvailability.AVAILABLE

    override fun FiniteAutomaton.doPerformOn(actionSubject: Unit) {
        if (automaton.isDeterministic) throw ActionFailedException(I18N.messages.getString("AutomatonDeterminization.AlreadyDeterministic"))
        if (automaton.hasEpsilon) throw ActionFailedException(
            MessageFormat.format(
                I18N.messages.getString("AutomatonDeterminization.EliminateEpsilonFirst"),
                I18N.messages.getString("Action.EliminateEpsilonTransition")
            )
        )
        if (automaton.initialVertices.isEmpty()) throw ActionFailedException(I18N.messages.getString("AutomatonDeterminization.AddInitStates"))
        if (automaton.buildingBlocks.isNotEmpty()) throw ActionFailedException(I18N.messages.getString("AutomatonDeterminization.NotSupportedForBuildingBlocks"))
        AutomatonDeterminization(automaton).start()
    }
}
