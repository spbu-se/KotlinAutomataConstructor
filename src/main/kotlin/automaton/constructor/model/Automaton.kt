package automaton.constructor.model

import automaton.constructor.model.module.AutomatonModule
import automaton.constructor.model.transition.Transition
import automaton.constructor.model.transition.storage.TransitionStorage
import automaton.constructor.model.transition.storage.createTransitionStorage
import javafx.collections.ObservableSet
import tornadofx.*

class Automaton(
    val memory: List<MemoryUnit>
) {
    private val transitionStorages = mutableMapOf<State, TransitionStorage>()
    private val outgoingTransitions = mutableMapOf<State, ObservableSet<Transition>>()
    private val incomingTransitions = mutableMapOf<State, MutableSet<Transition>>()
    private val modules = mutableMapOf<(Automaton) -> AutomatonModule, AutomatonModule>()
    private var nextId = 0

    init {
        val memoryCounts = mutableMapOf<String, Int>()
        memory.forEach { memoryUnit ->
            memoryCounts.compute(memoryUnit.name) { _, count ->
                if (count == null) 1
                else {
                    memoryUnit.name += " ${count + 1}"
                    count + 1
                }
            }
        }
    }

    val transitions = observableSetOf<Transition>()
    val states = observableSetOf<State>()

    fun getPossibleTransitions(state: State, memory: List<MemoryUnit>): Set<Transition> =
        transitionStorages[state]?.getPossibleTransitions(memory.flatMap { it.getCurrentFilterValues() }) ?: emptySet()

    fun getPureTransitions(state: State): Set<Transition> =
        transitionStorages[state]?.getPureTransitions() ?: emptySet()

    fun getTransitions(state: State) = outgoingTransitions.getValue(state)

    fun addState(state: State) {
        if (state.nameProperty.value == "") state.nameProperty.value = "S${nextId++}"
        transitionStorages[state] = createTransitionStorage(memory)
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
        val transition = Transition(source, target, memory)
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
