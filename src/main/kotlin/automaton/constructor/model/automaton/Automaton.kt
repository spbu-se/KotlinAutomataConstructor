package automaton.constructor.model.automaton

import automaton.constructor.model.action.AutomatonElementAction
import automaton.constructor.model.data.AutomatonTypeData
import automaton.constructor.model.element.AutomatonVertex
import automaton.constructor.model.element.BuildingBlock
import automaton.constructor.model.element.State
import automaton.constructor.model.element.Transition
import automaton.constructor.model.memory.MemoryUnit
import automaton.constructor.model.memory.MemoryUnitDescriptor
import automaton.constructor.model.module.AutomatonModule
import automaton.constructor.utils.UndoRedoManager
import javafx.collections.ObservableSet
import javafx.geometry.Point2D

/**
 * An automaton that has:
 *  - the [type name][typeDisplayName]
 *  - modifiable multigraph with vertices of type [AutomatonVertex] and edges of type [Transition]
 *  - fixed list of [MemoryUnitDescriptor]-s
 *  - dynamically extendable set of [AutomatonModule]-s
 */
interface Automaton {
    val typeDisplayName: String
    val memoryDescriptors: List<MemoryUnitDescriptor>

    val undoRedoManager: UndoRedoManager

    val transitions: ObservableSet<Transition>
    val vertices: ObservableSet<AutomatonVertex>
    val states: ObservableSet<State>
    val buildingBlocks: ObservableSet<BuildingBlock>

    val deterministicAdjective: String
    val nondeterministicAdjective: String
    val untitledAdjective: String

    /**
     * Returns the [type data][AutomatonTypeData] of this automaton.
     */
    fun getTypeData(): AutomatonTypeData


    /**
     * Returns all possible transitions from a given [vertex] given [memory] data
     */
    fun getPossibleTransitions(vertex: AutomatonVertex, memory: List<MemoryUnit>): Set<Transition>

    /**
     * Returns all pure transitions from a given [vertex]
     * @see Transition.isPure
     */
    fun getPureTransitions(vertex: AutomatonVertex): Set<Transition>

    /**
     * Returns set containing all transition to a given [vertex]
     */
    fun getIncomingTransitions(vertex: AutomatonVertex): Set<Transition>

    /**
     * Returns observable set containing all transition from a given [vertex]
     *
     * Returned set is modified whenever the set of transitions from [vertex] changes
     */
    fun getOutgoingTransitions(vertex: AutomatonVertex): ObservableSet<Transition>

    /**
     * Adds transition from [source] to [target]
     * @return added transition
     */
    fun addTransition(source: AutomatonVertex, target: AutomatonVertex): Transition

    fun removeTransition(transition: Transition)


    fun addState(name: String? = null, position: Point2D = Point2D.ZERO): State

    fun addBuildingBlock(
        subAutomaton: Automaton = createSubAutomaton(),
        name: String? = null,
        position: Point2D = Point2D.ZERO
    ): BuildingBlock

    fun createSubAutomaton(): Automaton

    fun removeVertex(vertex: AutomatonVertex)

    /**
     * The list of actions that can be possibly performed on a transition of the automaton.
     */
    val transitionActions: List<AutomatonElementAction<Transition>>

    /**
     * The list of actions that can be possibly performed on a state of the automaton.
     */
    val stateActions: List<AutomatonElementAction<State>>

    /**
     * The list of actions that can be possibly performed on a building block of the automaton.
     */
    val buildingBlockActions: List<AutomatonElementAction<BuildingBlock>>

    /**
     * Clears execution states for every vertex of this automaton and its sub-automatons
     */
    fun clearExecutionStates()

    /**
     * Returns [AutomatonModule] created by given [moduleFactory]
     *
     * If the given [moduleFactory] has been previously used to get this automaton module then
     * it's not invoked again and cached result of the previous invocation is returned
     */
    fun <T : AutomatonModule> getModule(moduleFactory: (Automaton) -> T): T
}

// Get automaton characteristics

val Automaton.allowsBuildingBlocks get() = memoryDescriptors.all { it.isAlwaysReadyToTerminate }
val Automaton.allowsStepByClosure get() = memoryDescriptors.all { it.allowsStepByClosure }

// Get specific transitions

/**
 * Returns set containing all loops of a given [vertex]
 */
fun Automaton.getLoops(vertex: AutomatonVertex): Set<Transition> =
    getIncomingTransitions(vertex).filterTo(mutableSetOf(), Transition::isLoop)

/**
 * Returns set containing all transition of a given [vertex]
 */
fun Automaton.getTransitions(vertex: AutomatonVertex): Set<Transition> =
    getIncomingTransitions(vertex) + getOutgoingTransitions(vertex)

/**
 * Returns set containing all non-loop transition of a given [vertex]
 */
fun Automaton.getTransitionsWithoutLoops(vertex: AutomatonVertex): Set<Transition> =
    getTransitions(vertex).filterNotTo(mutableSetOf(), Transition::isLoop)

/**
 * Returns set containing all transition to a given [vertex] without loops
 */
fun Automaton.getIncomingTransitionsWithoutLoops(vertex: AutomatonVertex): Set<Transition> =
    getIncomingTransitions(vertex).filterNotTo(mutableSetOf(), Transition::isLoop)

/**
 * Returns set containing all transition from a given [vertex] without loops
 */
fun Automaton.getOutgoingTransitionsWithoutLoops(vertex: AutomatonVertex): Set<Transition> =
    getOutgoingTransitions(vertex).filterNotTo(mutableSetOf(), Transition::isLoop)


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
    newSource: AutomatonVertex? = null, newTarget: AutomatonVertex? = null,
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
