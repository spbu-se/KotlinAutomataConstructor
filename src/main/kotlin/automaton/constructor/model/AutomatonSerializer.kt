package automaton.constructor.model

import automaton.constructor.model.memory.MemoryUnitDescriptor
import automaton.constructor.utils.surrogateSerializer
import javafx.geometry.Point2D
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

object AutomatonSerializer : KSerializer<Automaton> by surrogateSerializer(
    { automaton ->
        val stateToIdMap = automaton.states.mapIndexed { i, state -> state to i }.associate { it }
        AutomatonData(
            typeName = automaton.typeName,
            memoryDescriptors = automaton.memoryDescriptors,
            states = automaton.states.mapIndexed { i, state ->
                StateData(
                    id = i,
                    name = state.name,
                    isInitial = state.isInitial,
                    isFinal = state.isFinal,
                    x = state.position.x,
                    y = state.position.y,
                    properties = state.readProperties()
                )
            },
            transitions = automaton.transitions.map { transition ->
                TransitionData(
                    source = stateToIdMap.getValue(transition.source),
                    target = stateToIdMap.getValue(transition.target),
                    properties = transition.readProperties()
                )
            }
        )
    },
    { data ->
        Automaton(data.typeName, data.memoryDescriptors).also { automaton ->
            val idToStateMap = data.states.associate {
                it.id to automaton.addState(it.name, Point2D(it.x, it.y)).apply {
                    isInitial = it.isInitial
                    isFinal = it.isFinal
                    writeProperties(it.properties)
                }
            }
            data.transitions.forEach {
                automaton.addTransition(idToStateMap.getValue(it.source), idToStateMap.getValue(it.target)).apply {
                    writeProperties(it.properties)
                }
            }
            automaton.undoRedoManager.reset()
        }
    }
)

@Serializable
data class AutomatonData(
    val typeName: String,
    val memoryDescriptors: List<MemoryUnitDescriptor>,
    val states: List<StateData>,
    val transitions: List<TransitionData>
)

@Serializable
data class StateData(
    val id: Int,
    val name: String,
    val isInitial: Boolean = false,
    val isFinal: Boolean = false,
    val x: Double,
    val y: Double,
    val properties: List<String> = emptyList()
)

@Serializable
data class TransitionData(
    val source: Int,
    val target: Int,
    val properties: List<String> = emptyList()
)
