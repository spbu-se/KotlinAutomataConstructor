package automaton.constructor.model.transition.storage

import automaton.constructor.model.transition.Transition
import javafx.beans.property.Property

interface TransitionStorage {
    val isEmptyProperty: Property<Boolean>
    val isEmpty: Boolean

    fun addTransition(transition: Transition)
    fun removeTransition(transition: Transition)
    fun getPossibleTransitions(memoryState: List<*>): Set<Transition>
    fun getPureTransitions(): Set<Transition>
}
