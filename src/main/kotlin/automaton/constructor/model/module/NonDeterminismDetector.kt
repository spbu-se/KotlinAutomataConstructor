package automaton.constructor.model.module

import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.transition.Transition
import automaton.constructor.utils.filteredSet
import javafx.beans.binding.Bindings.isEmpty
import javafx.beans.binding.Bindings.size
import javafx.beans.binding.BooleanBinding
import javafx.collections.SetChangeListener
import tornadofx.*

private val nonDeterminismDetectorFactory = { automaton: Automaton -> NonDeterminismDetector(automaton) }
val Automaton.nonDeterminismDetector get() = getModule(nonDeterminismDetectorFactory)
val Automaton.isDeterministicBinding get() = nonDeterminismDetector.isDeterministicBinding

class NonDeterminismDetector(automaton: Automaton) : AutomatonModule {
    val nonDeterministicStates = automaton.states.filteredSet { state ->
        val isNonDeterministic = true.toProperty()
        val transitions = automaton.getOutgoingTransitions(state)
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
        isNonDeterministic
    }
    val isDeterministicBinding: BooleanBinding =
        isEmpty(nonDeterministicStates).and(size(automaton.initialStates).booleanBinding {
            automaton.initialStates.size <= 1
        })
}
