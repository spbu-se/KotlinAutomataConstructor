package automaton.constructor.model.transition.storage

import automaton.constructor.model.element.Transition
import automaton.constructor.model.property.DynamicProperty
import automaton.constructor.model.property.EPSILON_VALUE
import javafx.beans.value.ChangeListener
import tornadofx.*

class BranchTransitionStorage(
    private val depth: Int,
    private val subStorageFactory: (Int) -> TransitionStorage
) : TransitionStorage {
    private val subStorages = mutableMapOf<Any?, TransitionStorage>()
    private val transitionToListenerMap = mutableMapOf<Transition, ChangeListener<Any?>>()
    override val isEmptyProperty = true.toProperty()
    override var isEmpty by isEmptyProperty

    override fun addTransition(transition: Transition) {
        getOrCreateSubStorage(transition.filters[depth]).addTransition(transition)
        val listener = ChangeListener<Any?> { _, oldValue, newValue ->
            val newSubStorage = getOrCreateSubStorageByValue(newValue)
            subStorages[oldValue]!!.removeTransition(transition)
            newSubStorage.addTransition(transition)
        }
        transitionToListenerMap[transition] = listener
        transition.filters[depth].addListener(listener)
        isEmpty = false
    }

    override fun removeTransition(transition: Transition) {
        transition.filters[depth].removeListener(transitionToListenerMap.remove(transition)!!)
        subStorages[transition.filters[depth].value]!!.removeTransition(transition)
    }

    override fun getPossibleTransitions(memoryData: List<*>): Set<Transition> =
        (subStorages[EPSILON_VALUE]?.getPossibleTransitions(memoryData) ?: emptySet()) +
                (subStorages[memoryData[depth]]?.getPossibleTransitions(memoryData) ?: emptySet())

    override fun getPureTransitions(): Set<Transition> =
        subStorages[EPSILON_VALUE]?.getPureTransitions() ?: emptySet()

    private fun getOrCreateSubStorage(filter: DynamicProperty<*>): TransitionStorage =
        getOrCreateSubStorageByValue(filter.value)

    private fun getOrCreateSubStorageByValue(filterValue: Any?): TransitionStorage = subStorages.getOrPut(filterValue) {
        val newSubStorage = subStorageFactory(depth + 1)
        newSubStorage.isEmptyProperty.addListener { _, _, newValue ->
            if (newValue) {
                subStorages.remove(filterValue)
                if (subStorages.isEmpty()) isEmpty = true
            }
        }
        newSubStorage
    }
}
