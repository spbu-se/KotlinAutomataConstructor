package automaton.constructor.model.data

import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.element.AutomatonVertex
import automaton.constructor.model.element.BuildingBlock
import automaton.constructor.model.element.State
import automaton.constructor.utils.MostlyGeneratedOrInline
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class AutomatonVertexData {
    abstract val id: Int
    abstract val name: String
    abstract val x: Double
    abstract val y: Double
    abstract val isInitial: Boolean
    abstract val isFinal: Boolean
    abstract val requiresLayout: Boolean
}

/**
 * The data of a [state][State].
 *
 * It consists of an [id], a [name], [isInitial] and [isFinal] statuses, [x] and [y] coordinates, and a list of [properties].
 * The data can be converted to a state with the appropriate [name][State.name],
 * [isInitial][State.isInitial] and [isFinal][State.isFinal] statuses,
 * [position][State.position], and [dynamic properties][State.properties].
 */
@MostlyGeneratedOrInline
@Serializable
@SerialName("state")
data class StateData(
    override val id: Int,
    override val name: String,
    override val x: Double,
    override val y: Double,
    override val isInitial: Boolean = false,
    override val isFinal: Boolean = false,
    override val requiresLayout: Boolean = false,
    val properties: List<String> = emptyList()
) : AutomatonVertexData()

@MostlyGeneratedOrInline
@Serializable
@SerialName("building-block")
data class BuildingBlockData(
    override val id: Int,
    override val name: String,
    override val x: Double,
    override val y: Double,
    override val isInitial: Boolean = false,
    override val isFinal: Boolean = false,
    override val requiresLayout: Boolean = false,
    val vertices: Set<AutomatonVertexData>,
    val transitions: Set<TransitionData>,
    val edges: Set<AutomatonEdgeData> = emptySet()
) : AutomatonVertexData()

/**
 * Retrieves [data][AutomatonVertexData] for all [vertices][Automaton.vertices] os the automaton.
 */
fun Automaton.getVerticesData(vertexToIdMap: Map<AutomatonVertex, Int>): Set<AutomatonVertexData> = vertices.map { vertex ->
    when (vertex) {
        is State -> StateData(
            id = vertexToIdMap.getValue(vertex),
            name = vertex.name,
            x = vertex.position.x,
            y = vertex.position.y,
            isInitial = vertex.isInitial,
            isFinal = vertex.isFinal,
            requiresLayout = vertex.requiresLayout,
            properties = vertex.readProperties()
        )
        is BuildingBlock -> {
            val automatonData = vertex.subAutomaton.getData()
            BuildingBlockData(
                id = vertexToIdMap.getValue(vertex),
                name = vertex.name,
                x = vertex.position.x,
                y = vertex.position.y,
                isInitial = vertex.isInitial,
                isFinal = vertex.isFinal,
                requiresLayout = vertex.requiresLayout,
                vertices = automatonData.vertices,
                transitions = automatonData.transitions,
                edges = automatonData.edges
            )
        }
    }
}.toSet()
