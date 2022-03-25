package automaton.constructor.model

import automaton.constructor.model.memory.MemoryUnit
import automaton.constructor.model.memory.MemoryUnitDescriptor
import automaton.constructor.model.module.AutomatonModule
import automaton.constructor.model.property.EPSILON_VALUE
import automaton.constructor.model.transition.Transition
import automaton.constructor.model.transition.storage.TransitionStorage
import automaton.constructor.model.transition.storage.createTransitionStorageTree
import automaton.constructor.utils.UndoRedoManager
import javafx.collections.ObservableSet
import javafx.geometry.Point2D
import tornadofx.*

/**
 * Automaton that contains
 *  - Fixed [typeName]
 *  - Modifiable graph with vertices of type [State] and edges of type [Transition]
 *  - Fixed list of [MemoryUnitDescriptor]-s
 *  - Dynamically extendable set of [AutomatonModule]-s
 */
class Automaton(
    val typeName: String,
    val memoryDescriptors: List<MemoryUnitDescriptor>
) {
    private val transitionStorages = mutableMapOf<State, TransitionStorage>()
    private val outgoingTransitions = mutableMapOf<State, ObservableSet<Transition>>()
    private val incomingTransitions = mutableMapOf<State, MutableSet<Transition>>()
    private val modules = mutableMapOf<(Automaton) -> AutomatonModule, AutomatonModule>()
    private var nextStateSuffix = 0

    val undoRedoManager = UndoRedoManager()

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

    /**
     * Returns all possible transitions from a given [state] given [memory] data
     */
    fun getPossibleTransitions(state: State, memory: List<MemoryUnit>): Set<Transition> =
        transitionStorages[state]?.getPossibleTransitions(memory.flatMap {
            if (it.status.noMoreDataAvailable) (it.descriptor.transitionFilters + it.descriptor.stateFilters).map { EPSILON_VALUE }
            else it.getCurrentFilterValues()
        }) ?: emptySet()

    /**
     * Returns all pure transitions from a given [state]
     * @see Transition.isPure
     */
    fun getPureTransitions(state: State): Set<Transition> =
        transitionStorages[state]?.getPureTransitions() ?: emptySet()

    /**
     * Returns observable set containing all transition from a given [state]
     *
     * Returned set is modified whenever the set of transitions from [state] changes
     */
    fun getTransitions(state: State): ObservableSet<Transition> = outgoingTransitions.getValue(state)

    fun addState(name: String? = null, position: Point2D = Point2D.ZERO): State {
        val state = State(name ?: "S${nextStateSuffix++}", position, memoryDescriptors)
        undoRedoManager.perform({ doAddState(state) }, { doRemoveState(state) })
        return state
    }

    fun removeState(state: State) = undoRedoManager.perform({ doRemoveState(state) }, { doAddState(state) })


    private fun doAddState(state: State) {
        transitionStorages[state] = createTransitionStorageTree(memoryDescriptors)
        outgoingTransitions[state] = observableSetOf()
        incomingTransitions[state] = mutableSetOf()
        state.undoRedoProperties.forEach { undoRedoManager.registerProperty(it) }
        states.add(state)
    }

    private fun doRemoveState(state: State) {
        outgoingTransitions.getValue(state).toList().forEach { removeTransition(it) }
        incomingTransitions.getValue(state).toList().forEach { removeTransition(it) }
        transitionStorages.remove(state)
        outgoingTransitions.remove(state)
        incomingTransitions.remove(state)
        state.undoRedoProperties.forEach { undoRedoManager.unregisterProperty(it) }
        states.remove(state)
    }

    /**
     * Adds transition from [source] to [target]
     * @return added transition
     */
    fun addTransition(source: State, target: State): Transition {
        val transition = Transition(source, target, memoryDescriptors)
        undoRedoManager.perform({ doAddTransition(transition) }, { doRemoveTransition(transition) })
        return transition
    }

    fun removeTransition(transition: Transition) =
        undoRedoManager.perform({ doRemoveTransition(transition) }, { doAddTransition(transition) })

    private fun doAddTransition(transition: Transition) {
        transitionStorages.getValue(transition.source).addTransition(transition)
        outgoingTransitions.getValue(transition.source).add(transition)
        incomingTransitions.getValue(transition.target).add(transition)
        transition.undoRedoProperties.forEach { undoRedoManager.registerProperty(it) }
        transitions.add(transition)
    }

    private fun doRemoveTransition(transition: Transition) {
        transitionStorages.getValue(transition.source).removeTransition(transition)
        outgoingTransitions.getValue(transition.source).remove(transition)
        incomingTransitions.getValue(transition.target).remove(transition)
        transition.undoRedoProperties.forEach { undoRedoManager.unregisterProperty(it) }
        transitions.remove(transition)
    }

    /**
     * Returns [AutomatonModule] created by given [moduleFactory]
     *
     * If the given [moduleFactory] has been previously used to get this automaton module then
     * it's not invoked again and cached result of the previous invocation is returned
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : AutomatonModule> getModule(moduleFactory: (Automaton) -> T): T =
        modules.getOrPut(moduleFactory) { moduleFactory(this) } as T
}
