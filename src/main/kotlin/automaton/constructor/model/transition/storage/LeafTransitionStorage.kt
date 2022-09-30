package automaton.constructor.model.transition.storage

import automaton.constructor.model.element.Transition
import tornadofx.*

class LeafTransitionStorage : TransitionStorage {
    override val isEmptyProperty = true.toProperty()
    override val isEmpty by isEmptyProperty

    private val transitions = mutableSetOf<Transition>()

    override fun addTransition(transition: Transition) {
        transitions.add(transition)
        isEmptyProperty.value = false
    }

    override fun removeTransition(transition: Transition) {
        transitions.remove(transition)
        if (transitions.isEmpty()) isEmptyProperty.value = true
    }

    override fun getPossibleTransitions(memoryData: List<*>) = transitions

    override fun getPureTransitions(): Set<Transition> =
        transitions.filterTo(mutableSetOf()) { it.isPure() }
}
