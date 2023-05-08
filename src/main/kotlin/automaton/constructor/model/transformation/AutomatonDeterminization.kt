package automaton.constructor.model.transformation

import automaton.constructor.model.action.AbstractAction
import automaton.constructor.model.action.ActionAvailability
import automaton.constructor.model.action.ActionFailedException
import automaton.constructor.model.automaton.FiniteAutomaton
import automaton.constructor.model.automaton.GRAPH_PANE_CENTER
import automaton.constructor.model.automaton.getClosure
import automaton.constructor.model.element.AutomatonElement
import automaton.constructor.model.element.AutomatonVertex
import automaton.constructor.model.element.State
import automaton.constructor.model.module.initialVertices
import automaton.constructor.model.module.isDeterministic
import automaton.constructor.utils.I18N
import javafx.beans.binding.Bindings.isEmpty
import javafx.collections.SetChangeListener
import javafx.geometry.Point2D
import javafx.scene.transform.Rotate
import tornadofx.Vector2D
import tornadofx.observableSetOf
import tornadofx.plus
import kotlin.collections.Set
import kotlin.collections.any
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.filter
import kotlin.collections.first
import kotlin.collections.flatMap
import kotlin.collections.forEach
import kotlin.collections.getOrPut
import kotlin.collections.getValue
import kotlin.collections.groupBy
import kotlin.collections.isNotEmpty
import kotlin.collections.joinToString
import kotlin.collections.map
import kotlin.collections.mutableMapOf
import kotlin.collections.set
import kotlin.collections.sorted
import kotlin.collections.toSet

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
                .flatMap { state ->
                    val closure = nfa.getClosure(state as State)
                    stepSubject.isFinal = stepSubject.isFinal || closure.any { it.isFinal }
                    closure.flatMap { closureElm ->
                        nfa.getOutgoingTransitions(closureElm).filter { !it.isPure() }
                    }
                }
                .groupBy(keySelector = { it[nfa.inputTape.expectedChar] }, valueTransform = { it.target })
                .also { if (it.isNotEmpty()) stepSubject.requiresLayout = true }
                .forEach { (char, targets) ->
                    val combinedTarget = getOrCreateCombinedState(targets.toSet()) {
                        stepSubject.position +
                                Rotate(nextTransitionAngleDegrees.also { nextTransitionAngleDegrees += 48.0 })
                                    .transform(Vector2D(0.5 * AutomatonVertex.RADIUS, 0.0))
                    }
                    combinedTarget.requiresLayout = true
                    dfa.addTransition(stepSubject, combinedTarget)[dfa.inputTape.expectedChar] = char
                }
            unexpandedStates.remove(stepSubject)
        }
    }

    override fun complete() {
        while (unexpandedStates.isNotEmpty())
            step(unexpandedStates.first())
    }

    private fun getOrCreateCombinedState(
        vertices: Set<AutomatonVertex>,
        positionProvider: () -> Point2D = { GRAPH_PANE_CENTER }
    ) = statesToCombinedStateMap.getOrPut(vertices) {
        dfa.addState(
            name = vertices.map { it.name }.sorted().joinToString(separator = ","),
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
        if (isDeterministic) throw ActionFailedException(I18N.messages.getString("AutomatonDeterminization.AlreadyDeterministic"))
        if (initialVertices.isEmpty()) throw ActionFailedException(I18N.messages.getString("AutomatonDeterminization.AddInitStates"))
        if (buildingBlocks.isNotEmpty()) throw ActionFailedException(I18N.messages.getString("AutomatonDeterminization.NotSupportedForBuildingBlocks"))
        AutomatonDeterminization(automaton).start()
    }
}
