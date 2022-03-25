package automaton.constructor.model

import automaton.constructor.model.memory.MemoryUnitDescriptor
import javafx.geometry.Point2D
import kotlinx.serialization.Serializable

fun AutomatonData.toAutomaton() =
    Automaton(typeName, memoryDescriptors).also { automaton ->
        val idToStateMap = states.associate {
            it.id to automaton.addState(it.name, Point2D(it.x, it.y)).apply {
                isInitial = it.isInitial
                isFinal = it.isFinal
                writeProperties(it.properties)
            }
        }
        transitions.forEach {
            automaton.addTransition(idToStateMap.getValue(it.source), idToStateMap.getValue(it.target)).apply {
                writeProperties(it.properties)
            }
        }
        automaton.undoRedoManager.reset()
    }

fun Automaton.toData(): AutomatonData {
    val stateToIdMap = states.mapIndexed { i, state -> state to i }.associate { it }
    return AutomatonData(
        typeName = typeName,
        memoryDescriptors = memoryDescriptors,
        states = states.mapIndexed { i, state ->
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
        transitions = transitions.map { transition ->
            TransitionData(
                source = stateToIdMap.getValue(transition.source),
                target = stateToIdMap.getValue(transition.target),
                properties = transition.readProperties()
            )
        }
    )
}

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
