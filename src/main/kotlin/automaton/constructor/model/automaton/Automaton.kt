package automaton.constructor.model.automaton

import automaton.constructor.model.State
import automaton.constructor.model.action.AutomatonElementAction
import automaton.constructor.model.data.AutomatonTypeData
import automaton.constructor.model.memory.MemoryUnit
import automaton.constructor.model.memory.MemoryUnitDescriptor
import automaton.constructor.model.module.AutomatonModule
import automaton.constructor.model.transition.Transition
import automaton.constructor.utils.UndoRedoManager
import javafx.collections.ObservableSet
import javafx.geometry.Point2D

/**
 * Automaton
 *
 * It has:
 *  - the [type name][typeName]
 *  - modifiable graph with vertices of type [State] and edges of type [Transition]
 *  - fixed list of [MemoryUnitDescriptor]-s
 *  - dynamically extendable set of [AutomatonModule]-s
 */
interface Automaton {
    val typeName: String
    val memoryDescriptors: List<MemoryUnitDescriptor>

    val undoRedoManager: UndoRedoManager

    val transitions: ObservableSet<Transition>
    val states: ObservableSet<State>

    fun getTypeDataOrNull(): AutomatonTypeData?


    /**
     * Returns all possible transitions from a given [state] given [memory] data
     */
    fun getPossibleTransitions(state: State, memory: List<MemoryUnit>): Set<Transition>

    /**
     * Returns all pure transitions from a given [state]
     * @see Transition.isPure
     */
    fun getPureTransitions(state: State): Set<Transition>

    /**
     * Returns observable set containing all transition from a given [state]
     *
     * Returned set is modified whenever the set of transitions from [state] changes
     */
    fun getTransitions(state: State): ObservableSet<Transition>

    /**
     * Adds transition from [source] to [target]
     * @return added transition
     */
    fun addTransition(source: State, target: State): Transition

    fun removeTransition(transition: Transition)


    fun addState(name: String? = null, position: Point2D = Point2D.ZERO): State

    fun removeState(state: State)


    /**
     * The list of actions that can be possibly performed on a transition of the automaton.
     */
    val transitionActions: List<AutomatonElementAction<Transition>>

    /**
     * The list of actions that can be possibly performed on a state of the automaton.
     */
    val stateActions: List<AutomatonElementAction<State>>


    /**
     * Returns [AutomatonModule] created by given [moduleFactory]
     *
     * If the given [moduleFactory] has been previously used to get this automaton module then
     * it's not invoked again and cached result of the previous invocation is returned
     */
    fun <T : AutomatonModule> getModule(moduleFactory: (Automaton) -> T): T
}
