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
 *  - the [type name][typeDisplayName]
 *  - modifiable graph with vertices of type [State] and edges of type [Transition]
 *  - fixed list of [MemoryUnitDescriptor]-s
 *  - dynamically extendable set of [AutomatonModule]-s
 */
interface Automaton {
    val typeDisplayName: String
    val memoryDescriptors: List<MemoryUnitDescriptor>

    val undoRedoManager: UndoRedoManager

    val transitions: ObservableSet<Transition>
    val states: ObservableSet<State>

    val deterministicAdjective: String
    val nondeterministicAdjective: String
    val untitledAdjective: String

    /**
     * Returns the [type data][AutomatonTypeData] of this automaton.
     */
    fun getTypeData(): AutomatonTypeData


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
     * Returns set containing all transition to a given [state]
     */
    fun getIncomingTransitions(state: State): Set<Transition>

    /**
     * Returns observable set containing all transition from a given [state]
     *
     * Returned set is modified whenever the set of transitions from [state] changes
     */
    fun getOutgoingTransitions(state: State): ObservableSet<Transition>

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


// Get specific transitions

/**
 * Returns set containing all loops of a given [state]
 */
fun Automaton.getLoops(state: State): Set<Transition> =
    getIncomingTransitions(state).filterTo(mutableSetOf(), Transition::isLoop)

/**
 * Returns set containing all transition of a given [state]
 */
fun Automaton.getTransitions(state: State): Set<Transition> =
    getIncomingTransitions(state) + getOutgoingTransitions(state)

/**
 * Returns set containing all non-loop transition of a given [state]
 */
fun Automaton.getTransitionsWithoutLoops(state: State): Set<Transition> =
    getTransitions(state).filterNotTo(mutableSetOf(), Transition::isLoop)

/**
 * Returns set containing all transition to a given [state] without loops
 */
fun Automaton.getIncomingTransitionsWithoutLoops(state: State): Set<Transition> =
    getIncomingTransitions(state).filterNotTo(mutableSetOf(), Transition::isLoop)

/**
 * Returns set containing all transition from a given [state] without loops
 */
fun Automaton.getOutgoingTransitionsWithoutLoops(state: State): Set<Transition> =
    getOutgoingTransitions(state).filterNotTo(mutableSetOf(), Transition::isLoop)


// Copy and add automaton elements

fun Automaton.copyAndAddState(
    state: State,
    newName: String? = null,
    newPosition: Point2D? = null,
    newIsInitial: Boolean? = null,
    newIsFinal: Boolean? = null
): State {
    val name = newName ?: state.name
    val position = newPosition ?: state.position
    return addState(name, position).apply {
        isInitial = newIsInitial ?: state.isInitial
        isFinal = newIsFinal ?: state.isFinal
        writeProperties(state.readProperties())
    }
}

fun Automaton.copyAndAddTransition(
    transition: Transition,
    newSource: State? = null, newTarget: State? = null,
    ignoreIfTransitionIsPureLoop: Boolean = false,
    ignoreIfCopyIsPureLoop: Boolean = false,
    ignoreIfCopyAlreadyExists: Boolean = false
): Transition? {
    val source = newSource ?: transition.source
    val target = newTarget ?: transition.target
    return when {
        ignoreIfTransitionIsPureLoop && transition.isLoop() && transition.isPure() -> null
        ignoreIfCopyIsPureLoop && source == target && transition.isPure() -> null
        ignoreIfCopyAlreadyExists && getOutgoingTransitions(source).any {
            it.target == target && it.readProperties() == transition.readProperties()
        } -> null
        else -> copyAndAddTransition(transition, newSource, newTarget)
    }
}

fun Automaton.copyAndAddTransition(
    transition: Transition,
    newSource: State? = null, newTarget: State? = null
): Transition {
    val source = newSource ?: transition.source
    val target = newTarget ?: transition.target
    return addTransition(source, target).apply { writeProperties(transition.readProperties()) }
}
