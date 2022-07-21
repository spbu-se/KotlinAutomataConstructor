package automaton.constructor.model.automaton

import automaton.constructor.model.State
import automaton.constructor.model.action.AutomatonElementAction
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
 * The base automaton for every other automaton.
 *
 * It has:
 *  - the fixed [type display name][typeDisplayName]
 *  - modifiable graph with vertices of type [State] and edges of type [Transition]
 *  - fixed list of [MemoryUnitDescriptor]-s
 *  - dynamically extendable set of [AutomatonModule]-s
 *
 * It's recommended to extend the `AbstractAutomaton` when creating new ones.
 */
abstract class AbstractAutomaton(
    final override val typeDisplayName: String,
    final override val memoryDescriptors: List<MemoryUnitDescriptor>,
) : Automaton {
    private val transitionStorages = mutableMapOf<State, TransitionStorage>()
    private val outgoingTransitions = mutableMapOf<State, ObservableSet<Transition>>()
    private val incomingTransitions = mutableMapOf<State, MutableSet<Transition>>()

    override val undoRedoManager = UndoRedoManager()

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

    override val transitions = observableSetOf<Transition>()
    override val states = observableSetOf<State>()

    private val nextStateSuffix: Int
        get() {
            val takenSuffixes = states
                .mapNotNull { GENERATED_STATE_NAME_REGEX.matchEntire(it.name) }
                .mapNotNull { it.groupValues[1].toIntOrNull() }
                .toSet()
            return generateSequence(0) { it + 1 }.first { it !in takenSuffixes }
        }


    override fun getPossibleTransitions(state: State, memory: List<MemoryUnit>): Set<Transition> =
        transitionStorages[state]?.getPossibleTransitions(memory.flatMap {
            if (it.status.noMoreDataAvailable) (it.descriptor.transitionFilters + it.descriptor.stateFilters).map { EPSILON_VALUE }
            else it.getCurrentFilterValues()
        }) ?: emptySet()

    override fun getPureTransitions(state: State): Set<Transition> =
        transitionStorages[state]?.getPureTransitions() ?: emptySet()

    override fun getTransitionsFrom(state: State): ObservableSet<Transition> = outgoingTransitions.getValue(state)

    override fun getTransitionsTo(state: State): Set<Transition> = incomingTransitions.getValue(state)

    override fun addTransition(source: State, target: State): Transition {
        val transition = Transition(source, target, memoryDescriptors)
        undoRedoManager.perform(
            act = { doAddTransition(transition) },
            undo = { doRemoveTransition(transition) }
        )
        return transition
    }

    override fun removeTransition(transition: Transition) {
        undoRedoManager.perform(
            act = { doRemoveTransition(transition) },
            undo = { doAddTransition(transition) }
        )
    }

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


    override fun addState(name: String?, position: Point2D): State {
        val state = State(name ?: (STATE_NAME_PREFIX + nextStateSuffix), position, memoryDescriptors)
        undoRedoManager.perform({ doAddState(state) }, { doRemoveState(state) })
        return state
    }

    override fun removeState(state: State) {
        undoRedoManager.perform(act = { doRemoveState(state) }, undo = { doAddState(state) })
    }

    private fun doAddState(state: State) {
        transitionStorages[state] = createTransitionStorageTree(memoryDescriptors)
        outgoingTransitions[state] = observableSetOf()
        incomingTransitions[state] = mutableSetOf()
        state.undoRedoProperties.forEach(undoRedoManager::registerProperty)
        states.add(state)
    }

    private fun doRemoveState(state: State) {
        outgoingTransitions.getValue(state).toList().forEach(::removeTransition)
        incomingTransitions.getValue(state).toList().forEach(::removeTransition)
        transitionStorages.remove(state)
        outgoingTransitions.remove(state)
        incomingTransitions.remove(state)
        state.undoRedoProperties.forEach(undoRedoManager::unregisterProperty)
        states.remove(state)
    }


    override val transitionActions: List<AutomatonElementAction<Transition>> = emptyList()

    override val stateActions: List<AutomatonElementAction<State>> = emptyList()


    @Suppress("UNCHECKED_CAST")
    override fun <T : AutomatonModule> getModule(moduleFactory: (Automaton) -> T): T =
        modules.getOrPut(moduleFactory) { moduleFactory(this) } as T

    private val modules = mutableMapOf<(Automaton) -> AutomatonModule, AutomatonModule>()


    companion object {
        private const val STATE_NAME_PREFIX = "S"
        private val GENERATED_STATE_NAME_REGEX = Regex("$STATE_NAME_PREFIX(\\d+)")
    }
}
