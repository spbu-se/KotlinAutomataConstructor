package automaton.constructor.model.automaton

import automaton.constructor.model.action.Action
import automaton.constructor.model.action.buildingblock.RemoveBuildingBlockAction
import automaton.constructor.model.action.state.MergeNondistinguishableStatesAction
import automaton.constructor.model.action.state.RemoveStateAction
import automaton.constructor.model.action.transition.RemoveTransitionAction
import automaton.constructor.model.element.*
import automaton.constructor.model.memory.MemoryUnit
import automaton.constructor.model.memory.MemoryUnitDescriptor
import automaton.constructor.model.memory.tape.MultiTrackTapeDescriptor
import automaton.constructor.model.module.AutomatonModule
import automaton.constructor.model.property.EPSILON_VALUE
import automaton.constructor.model.transformation.AutomatonTransformation
import automaton.constructor.model.transformation.MinimizeAction
import automaton.constructor.model.transition.storage.TransitionStorage
import automaton.constructor.model.transition.storage.createTransitionStorageTree
import automaton.constructor.utils.UndoRedoManager
import automaton.constructor.utils.filteredSet
import javafx.beans.binding.Bindings.concat
import javafx.collections.ObservableSet
import javafx.geometry.Point2D
import tornadofx.*

/**
 * The base automaton for every other automaton.
 *
 * It has:
 *  - the fixed [type display name][typeDisplayName]
 *  - modifiable multi-graph with vertices of type [AutomatonVertex] and edges of type [Transition]
 *      - parallel [Transition]s are grouped into [AutomatonEdge]s
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
    final override val nameProperty = untitledName.toProperty()
    final override var name: String by nameProperty

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
    override val edges = observableMapOf<Pair<AutomatonVertex, AutomatonVertex>, AutomatonEdge>()

    @Suppress("UNCHECKED_CAST")
    override val states = vertices.filteredSet { (it is State).toProperty() } as ObservableSet<State>

    @Suppress("UNCHECKED_CAST")
    override val buildingBlocks =
        vertices.filteredSet { (it is BuildingBlock).toProperty() } as ObservableSet<BuildingBlock>

    fun nextStateSuffix(): Int = nextVertexSuffix(GENERATED_STATE_NAME_REGEX)
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
        edges.getOrPut(transition.source to transition.target) {
            edges[transition.target to transition.source]?.resetRouting() // opposite edge
            AutomatonEdge(transition.source, transition.target).also { edge ->
                undoRedoManager.registerProperties(edge.undoRedoProperties)
            }
        }.transitions.add(transition)
        undoRedoManager.registerProperties(transition.undoRedoProperties)
        transitions.add(transition)
    }

    private fun doRemoveTransition(transition: Transition) {
        transitionStorages.getValue(transition.source).removeTransition(transition)
        outgoingTransitions.getValue(transition.source).remove(transition)
        incomingTransitions.getValue(transition.target).remove(transition)
        edges.getValue(transition.source to transition.target).transitions.let { edgeTransitions ->
            edgeTransitions.remove(transition)
            if (edgeTransitions.isEmpty()) edges.remove(transition.source to transition.target)?.let { edge ->
                undoRedoManager.unregisterProperties(edge.undoRedoProperties)
            }
        }
        undoRedoManager.unregisterProperties(transition.undoRedoProperties)
        transitions.remove(transition)
    }


    override fun addState(name: String?, position: Point2D): State {
        val state = State(memoryDescriptors, name ?: (STATE_NAME_PREFIX + nextStateSuffix()), position)
        addVertex(state)
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
        subAutomaton.nameProperty.bind(concat(nameProperty, " > ", buildingBlock.nameProperty))
        addVertex(buildingBlock)
        return buildingBlock
    }

    private fun addVertex(vertex: AutomatonVertex) {
        vertex.positionProperty.onChange {
            undoRedoManager.group {
                (getIncomingTransitions(vertex) + getOutgoingTransitions(vertex)).forEach {
                    edges[it.source to it.target]?.resetRouting()
                }
            }
        }
        undoRedoManager.perform({ doAddVertex(vertex) }, { doRemoveVertex(vertex) })
    }

    override fun removeVertex(vertex: AutomatonVertex) {
        undoRedoManager.perform(act = { doRemoveVertex(vertex) }, undo = { doAddVertex(vertex) })
    }

    private fun doAddVertex(vertex: AutomatonVertex) {
        transitionStorages[vertex] = createTransitionStorageTree(memoryDescriptors)
        outgoingTransitions[vertex] = observableSetOf()
        incomingTransitions[vertex] = mutableSetOf()
        undoRedoManager.registerProperties(vertex.undoRedoProperties)
        if (vertex is BuildingBlock) undoRedoManager.registerSubManager(vertex.subAutomaton.undoRedoManager)
        vertices.add(vertex)
    }

    private fun doRemoveVertex(vertex: AutomatonVertex) {
        outgoingTransitions.getValue(vertex).toList().forEach(::removeTransition)
        incomingTransitions.getValue(vertex).toList().forEach(::removeTransition)
        transitionStorages.remove(vertex)
        outgoingTransitions.remove(vertex)
        incomingTransitions.remove(vertex)
        undoRedoManager.unregisterProperties(vertex.undoRedoProperties)
        if (vertex is BuildingBlock) undoRedoManager.unregisterSubManager(vertex.subAutomaton.undoRedoManager)
        vertices.remove(vertex)
    }

    override val transitionActions: List<Action<Transition>> = listOf(
        RemoveTransitionAction(automaton = this)
    )

    override val stateActions: List<Action<State>> = listOf(
        RemoveStateAction(automaton = this),
        MergeNondistinguishableStatesAction(automaton = this)
    )

    override val buildingBlockActions: List<Action<BuildingBlock>> = listOf(
        RemoveBuildingBlockAction(automaton = this)
    )

    override val transformationActions: List<Action<Unit>> = listOf(
        MinimizeAction(automaton = this)
    )

    final override val isInputForTransformationProperty = null.toProperty<AutomatonTransformation>()
    final override var isInputForTransformation: AutomatonTransformation? by isInputForTransformationProperty

    final override val isOutputOfTransformationProperty = null.toProperty<AutomatonTransformation>()
    final override var isOutputOfTransformation: AutomatonTransformation? by isOutputOfTransformationProperty

    final override val allowsModificationsByUserProperty = true.toProperty().apply {
        bind(isInputForTransformationProperty.isNull.and(isOutputOfTransformationProperty.isNull))
    }
    final override val allowsModificationsByUser by allowsModificationsByUserProperty

    override fun clearExecutionStates() = vertices.forEach {
        it.executionStates.clear()
        if (it is BuildingBlock) it.subAutomaton.clearExecutionStates()
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : AutomatonModule> getModule(moduleFactory: (Automaton) -> T): T =
        modules.getOrPut(moduleFactory) { moduleFactory(this) } as T

    private val modules = mutableMapOf<(Automaton) -> AutomatonModule, AutomatonModule>()

    override fun canUseTheseDescriptors(newDescriptors: List<MemoryUnitDescriptor>): Boolean {
        if (newDescriptors.size != memoryDescriptors.size) {
            return false
        }
        for (index in memoryDescriptors.indices) {
            if (!memoryDescriptors[index].isCompatibleWithDescriptor(newDescriptors[index])) {
                return false
            }
        }
        return true
    }

    companion object {
        const val STATE_NAME_PREFIX = "S"
        private const val BUILDING_BLOCK_NAME_PREFIX = "M"
        private val GENERATED_STATE_NAME_REGEX = Regex("$STATE_NAME_PREFIX(\\d+)")
        private val GENERATED_BUILDING_BLOCK_NAME_REGEX = Regex("$BUILDING_BLOCK_NAME_PREFIX(\\d+)")
    }
}
