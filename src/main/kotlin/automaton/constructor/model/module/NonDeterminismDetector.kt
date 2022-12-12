package automaton.constructor.model.module

import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.element.BuildingBlock
import automaton.constructor.model.element.State
import automaton.constructor.model.element.Transition
import automaton.constructor.utils.filteredSet
import javafx.beans.binding.Bindings.*
import javafx.beans.binding.BooleanBinding
import javafx.collections.SetChangeListener
import tornadofx.*

private val nonDeterminismDetectorFactory = { automaton: Automaton -> NonDeterminismDetector(automaton) }
val Automaton.nonDeterminismDetector get() = getModule(nonDeterminismDetectorFactory)
val Automaton.isDeterministicBinding get() = nonDeterminismDetector.isDeterministicBinding
val Automaton.isDeterministic: Boolean get() = isDeterministicBinding.value

class NonDeterminismDetector(automaton: Automaton) : AutomatonModule {
    val nonDeterministicStates = automaton.vertices.filteredSet { vertex ->
        val isNonDeterministic = true.toProperty()
        val transitions = automaton.getOutgoingTransitions(vertex)
        fun recheckDeterminism() {
            isNonDeterministic.value = transitions.any { transition1 ->
                transitions.any { transition2 ->
                    transition1 !== transition2 && transition1.filters.map { it.value }
                        .zip(transition2.filters.map { it.value })
                        .all { (v1, v2) -> v1 == null || v2 == null || v1 == v2 }
                }
            }
        }
        recheckDeterminism()
        val listener = ChangeListener<Any?> { _, _, _ -> recheckDeterminism() }
        fun registerTransition(transition: Transition) = transition.filters.forEach { it.addListener(listener) }
        fun unregisterTransition(transition: Transition) = transition.filters.forEach { it.removeListener(listener) }

        transitions.forEach { registerTransition(it) }
        transitions.addListener(SetChangeListener {
            if (it.wasAdded()) registerTransition(it.elementAdded)
            if (it.wasRemoved()) unregisterTransition(it.elementRemoved)
            recheckDeterminism()
        })
        when (vertex) {
            is State -> isNonDeterministic
            is BuildingBlock -> isNonDeterministic.or(not(vertex.subAutomaton.isDeterministicBinding))
        }
    }
    val isDeterministicBinding: BooleanBinding =
        isEmpty(nonDeterministicStates).and(size(automaton.initialVertices).booleanBinding {
            automaton.initialVertices.size <= 1
        })
}
