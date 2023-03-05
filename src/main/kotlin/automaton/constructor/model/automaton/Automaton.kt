package automaton.constructor.model.automaton

import automaton.constructor.model.action.Action
import automaton.constructor.model.data.AutomatonTypeData
import automaton.constructor.model.element.*
import automaton.constructor.model.memory.MemoryUnit
import automaton.constructor.model.memory.MemoryUnitDescriptor
import automaton.constructor.model.module.AutomatonModule
import automaton.constructor.model.transformation.AutomatonTransformation
import automaton.constructor.utils.UndoRedoManager
import javafx.beans.property.BooleanProperty
import javafx.beans.property.Property
import javafx.collections.ObservableMap
import javafx.collections.ObservableSet
import javafx.geometry.Point2D
import tornadofx.*

/**
 * An automaton that has:
 *  - the [name]
 *  - the [type name][typeDisplayName]
 *  - modifiable multigraph with vertices of type [AutomatonVertex] and edges of type [Transition]
 *  - fixed list of [MemoryUnitDescriptor]-s
 *  - dynamically extendable set of [AutomatonModule]-s
 */
interface Automaton {
    val nameProperty: Property<String>
    var name: String

    val typeDisplayName: String
    val memoryDescriptors: List<MemoryUnitDescriptor>

    val undoRedoManager: UndoRedoManager

    val transitions: ObservableSet<Transition>
    val vertices: ObservableSet<AutomatonVertex>
    val edges: ObservableMap<Pair<AutomatonVertex, AutomatonVertex>, AutomatonEdge>
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


    fun addState(name: String? = null, position: Point2D = GRAPH_PANE_CENTER): State

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
    val transitionActions: List<Action<Transition>>

    /**
     * The list of actions that can be possibly performed on a state of the automaton.
     */
    val stateActions: List<Action<State>>

    /**
     * The list of actions that can be possibly performed on a building block of the automaton.
     */
    val buildingBlockActions: List<Action<BuildingBlock>>

    /**
     * The list of actions that can be possibly performed on the entire automaton
     * resulting in [isInputForTransformationProperty] being set to non-null value
     */
    val transformationActions: List<Action<Unit>>

    /**
     * [AutomatonTransformation] for which this [Automaton] is an input
     */
    val isInputForTransformationProperty: Property<AutomatonTransformation?>
    var isInputForTransformation: AutomatonTransformation?

    /**
     * [AutomatonTransformation] for which this [Automaton] is an output
     */
    val isOutputOfTransformationProperty: Property<AutomatonTransformation?>
    var isOutputOfTransformation: AutomatonTransformation?

    val allowsModificationsByUserProperty: BooleanProperty
    val allowsModificationsByUser: Boolean

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

fun Automaton.copyAndAddTransitionConditionally(
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
    newSource: AutomatonVertex? = null, newTarget: AutomatonVertex? = null
): Transition {
    val source = newSource ?: transition.source
    val target = newTarget ?: transition.target
    return addTransition(source, target).apply { writeProperties(transition.readProperties()) }
}

// other utilities

val Automaton.untitledName: String
    get() = listOf(untitledAdjective, typeDisplayName).joinToString(" ")

fun Automaton.resetHighlights() = vertices.forEach { it.isHighlighted = false }

fun Automaton.getClosure(state: State): Collection<AutomatonVertex> {
    val closure: MutableSet<AutomatonVertex> = mutableSetOf(state)
    val unhandledStates = mutableListOf(state)
    while (unhandledStates.isNotEmpty()) {
        val curState = unhandledStates.removeLast()
        getPureTransitions(curState).forEach { pureTransition ->
            if (closure.add(pureTransition.target) && pureTransition.target is State)
                unhandledStates.add(pureTransition.target)
        }
    }
    return closure
}

val Automaton.transformationOutput: Automaton? get() = isInputForTransformation?.resultingAutomaton

val GRAPH_PANE_INIT_SIZE = Point2D(1_000_000.0, 1_000_000.0)
val GRAPH_PANE_CENTER = GRAPH_PANE_INIT_SIZE / 2.0
