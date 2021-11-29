package automaton.constructor.model

import automaton.constructor.model.memory.MemoryUnit
import automaton.constructor.model.memory.MemoryUnitDescriptor
import automaton.constructor.model.memory.MemoryUnitStatus.REQUIRES_ACCEPTANCE
import automaton.constructor.model.memory.MemoryUnitStatus.REQUIRES_TERMINATION
import automaton.constructor.model.module.AutomatonModule
import automaton.constructor.model.transition.Transition
import automaton.constructor.model.transition.property.EPSILON_VALUE
import automaton.constructor.model.transition.storage.TransitionStorage
import automaton.constructor.model.transition.storage.createTransitionStorage
import javafx.collections.ObservableSet
import tornadofx.*

class Automaton(
    val memoryDescriptors: List<MemoryUnitDescriptor>
) {
    private val transitionStorages = mutableMapOf<State, TransitionStorage>()
    private val outgoingTransitions = mutableMapOf<State, ObservableSet<Transition>>()
    private val incomingTransitions = mutableMapOf<State, MutableSet<Transition>>()
    private val modules = mutableMapOf<(Automaton) -> AutomatonModule, AutomatonModule>()
    private var nextId = 0

    init {
        val memoryNameToCountMap = mutableMapOf<String, Int>()
        memoryDescriptors.forEach { memoryUnitDescriptor ->
            memoryNameToCountMap.compute(memoryUnitDescriptor.displayName) { _, count ->
                if (count == null) 1
                else {
                    memoryUnitDescriptor.displayName += " ${count + 1}"
                    count + 1
                }
            }
        }
    }

    val transitions = observableSetOf<Transition>()
    val states = observableSetOf<State>()

    fun getPossibleTransitions(state: State, memory: List<MemoryUnit>): Set<Transition> =
        transitionStorages[state]?.getPossibleTransitions(memory.flatMap {
            if (it.status == REQUIRES_TERMINATION || it.status == REQUIRES_ACCEPTANCE)
                it.descriptor.filters.map { EPSILON_VALUE }
            else it.getCurrentFilterValues()
        }) ?: emptySet()

    fun getPureTransitions(state: State): Set<Transition> =
        transitionStorages[state]?.getPureTransitions() ?: emptySet()

    fun getTransitions(state: State) = outgoingTransitions.getValue(state)

    fun addState(state: State) {
        if (state.nameProperty.value == "") state.nameProperty.value = "S${nextId++}"
        transitionStorages[state] = createTransitionStorage(memoryDescriptors)
        outgoingTransitions[state] = observableSetOf()
        incomingTransitions[state] = mutableSetOf()
        states.add(state)
    }

    fun removeState(state: State) {
        outgoingTransitions.getValue(state).toList().forEach { removeTransition(it) }
        incomingTransitions.getValue(state).toList().forEach { removeTransition(it) }
        transitionStorages.remove(state)
        outgoingTransitions.remove(state)
        incomingTransitions.remove(state)
        states.remove(state)
    }

    fun addTransition(source: State, target: State): Transition {
        val transition = Transition(source, target, memoryDescriptors)
        transitionStorages.getValue(source).addTransition(transition)
        outgoingTransitions.getValue(source).add(transition)
        incomingTransitions.getValue(target).add(transition)
        transitions.add(transition)
        return transition
    }

    fun removeTransition(transition: Transition) {
        transitionStorages.getValue(transition.source).removeTransition(transition)
        outgoingTransitions.getValue(transition.source).remove(transition)
        incomingTransitions.getValue(transition.target).remove(transition)
        transitions.remove(transition)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : AutomatonModule> getModule(moduleFactory: (Automaton) -> T): T =
        modules.getOrPut(moduleFactory) { moduleFactory(this) } as T
}
