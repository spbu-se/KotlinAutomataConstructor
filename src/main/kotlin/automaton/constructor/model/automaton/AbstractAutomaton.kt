package automaton.constructor.model.automaton

import automaton.constructor.model.action.AutomatonElementAction
import automaton.constructor.model.action.buildingblock.createRemoveBuildingBlockAction
import automaton.constructor.model.action.state.createRemoveStateAction
import automaton.constructor.model.action.transition.createRemoveTransitionAction
import automaton.constructor.model.element.AutomatonVertex
import automaton.constructor.model.element.BuildingBlock
import automaton.constructor.model.element.State
import automaton.constructor.model.element.Transition
import automaton.constructor.model.memory.MemoryUnit
import automaton.constructor.model.memory.MemoryUnitDescriptor
import automaton.constructor.model.module.AutomatonModule
import automaton.constructor.model.property.EPSILON_VALUE
import automaton.constructor.model.transition.storage.TransitionStorage
import automaton.constructor.model.transition.storage.createTransitionStorageTree
import automaton.constructor.utils.UndoRedoManager
import automaton.constructor.utils.filteredSet
import javafx.collections.ObservableSet
import javafx.geometry.Point2D
import tornadofx.*

/**
 * The base automaton for every other automaton.
 *
 * It has:
 *  - the fixed [type display name][typeDisplayName]
 *  - modifiable graph with vertices of type [AutomatonVertex] and edges of type [Transition]
 *  - fixed list of [MemoryUnitDescriptor]-s
 *  - dynamically extendable set of [AutomatonModule]-s
 *
 * It's recommended to extend the `AbstractAutomaton` when creating new ones.
 */
abstract class AbstractAutomaton(
    final override val typeDisplayName: String,
    final override val memoryDescriptors: List<MemoryUnitDescriptor>,
    final override val deterministicAdjective: String,
    final override val nondeterministicAdjective: String,
    final override val untitledAdjective: String
) : Automaton {
    private val transitionStorages = mutableMapOf<AutomatonVertex, TransitionStorage>()

    private val outgoingTransitions = mutableMapOf<AutomatonVertex, ObservableSet<Transition>>()
    private val incomingTransitions = mutableMapOf<AutomatonVertex, MutableSet<Transition>>()

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
    final override val vertices = observableSetOf<AutomatonVertex>()

    @Suppress("UNCHECKED_CAST")
    override val states = vertices.filteredSet { (it is State).toProperty() } as ObservableSet<State>

    @Suppress("UNCHECKED_CAST")
    override val buildingBlocks =
        vertices.filteredSet { (it is BuildingBlock).toProperty() } as ObservableSet<BuildingBlock>

    private fun nextStateSuffix(): Int = nextVertexSuffix(GENERATED_STATE_NAME_REGEX)
    private fun nextBuildingBlockSuffix(): Int = nextVertexSuffix(GENERATED_BUILDING_BLOCK_NAME_REGEX)

    private fun nextVertexSuffix(vertexNameRegex: Regex): Int {
        val takenSuffixes = vertices
            .mapNotNull { vertexNameRegex.matchEntire(it.name) }
            .mapNotNull { it.groupValues[1].toIntOrNull() }
            .toSet()
        return generateSequence(0) { it + 1 }.first { it !in takenSuffixes }
    }


    override fun getPossibleTransitions(vertex: AutomatonVertex, memory: List<MemoryUnit>): Set<Transition> =
        transitionStorages[vertex]?.getPossibleTransitions(
            memory.flatMap {
                if (it.status.noMoreDataAvailable) (it.descriptor.transitionFilters).map { EPSILON_VALUE }
                else it.getCurrentFilterValues()
            }
        ) ?: emptySet()

    override fun getPureTransitions(vertex: AutomatonVertex): Set<Transition> =
        transitionStorages[vertex]?.getPureTransitions() ?: emptySet()

    override fun getOutgoingTransitions(vertex: AutomatonVertex): ObservableSet<Transition> =
        outgoingTransitions.getValue(vertex)

    override fun getIncomingTransitions(vertex: AutomatonVertex): Set<Transition> = incomingTransitions.getValue(vertex)

    override fun addTransition(source: AutomatonVertex, target: AutomatonVertex): Transition {
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
        val state = State(memoryDescriptors, name ?: (STATE_NAME_PREFIX + nextStateSuffix()), position)
        undoRedoManager.perform({ doAddVertex(state) }, { doRemoveVertex(state) })
        return state
    }

    override fun addBuildingBlock(subAutomaton: Automaton, name: String?, position: Point2D): BuildingBlock {
        val buildingBlock =
            BuildingBlock(
                memoryDescriptors,
                subAutomaton,
                name ?: (BUILDING_BLOCK_NAME_PREFIX + nextBuildingBlockSuffix()),
                position
            )
        undoRedoManager.perform({ doAddVertex(buildingBlock) }, { doRemoveVertex(buildingBlock) })
        return buildingBlock
    }

    override fun removeVertex(vertex: AutomatonVertex) {
        undoRedoManager.perform(act = { doRemoveVertex(vertex) }, undo = { doAddVertex(vertex) })
    }

    private fun doAddVertex(vertex: AutomatonVertex) {
        transitionStorages[vertex] = createTransitionStorageTree(memoryDescriptors)
        outgoingTransitions[vertex] = observableSetOf()
        incomingTransitions[vertex] = mutableSetOf()
        vertex.undoRedoProperties.forEach(undoRedoManager::registerProperty)
        if (vertex is BuildingBlock) undoRedoManager.registerSubManager(vertex.subAutomaton.undoRedoManager)
        vertices.add(vertex)
    }

    private fun doRemoveVertex(vertex: AutomatonVertex) {
        outgoingTransitions.getValue(vertex).toList().forEach(::removeTransition)
        incomingTransitions.getValue(vertex).toList().forEach(::removeTransition)
        transitionStorages.remove(vertex)
        outgoingTransitions.remove(vertex)
        incomingTransitions.remove(vertex)
        vertex.undoRedoProperties.forEach(undoRedoManager::unregisterProperty)
        if (vertex is BuildingBlock) undoRedoManager.unregisterSubManager(vertex.subAutomaton.undoRedoManager)
        vertices.remove(vertex)
    }

    override val transitionActions: List<AutomatonElementAction<Transition>> = listOf(
        createRemoveTransitionAction(automaton = this)
    )

    override val stateActions: List<AutomatonElementAction<State>> = listOf(
        createRemoveStateAction(automaton = this)
    )

    override val buildingBlockActions: List<AutomatonElementAction<BuildingBlock>> = listOf(
        createRemoveBuildingBlockAction(automaton = this)
    )

    override fun clearExecutionStates() = vertices.forEach {
        it.executionStates.clear()
        if (it is BuildingBlock) it.subAutomaton.clearExecutionStates()
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : AutomatonModule> getModule(moduleFactory: (Automaton) -> T): T =
        modules.getOrPut(moduleFactory) { moduleFactory(this) } as T

    private val modules = mutableMapOf<(Automaton) -> AutomatonModule, AutomatonModule>()


    companion object {
        private const val STATE_NAME_PREFIX = "S"
        private const val BUILDING_BLOCK_NAME_PREFIX = "M"
        private val GENERATED_STATE_NAME_REGEX = Regex("$STATE_NAME_PREFIX(\\d+)")
        private val GENERATED_BUILDING_BLOCK_NAME_REGEX = Regex("$BUILDING_BLOCK_NAME_PREFIX(\\d+)")
    }
}
