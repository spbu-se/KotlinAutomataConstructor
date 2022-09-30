package automaton.constructor.model.data

import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.element.BuildingBlock
import automaton.constructor.model.element.State
import automaton.constructor.utils.IgnorableByCoverage
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
}

/**
 * The data of a [state][State].
 *
 * It consists of an [id], a [name], [isInitial] and [isFinal] statuses, [x] and [y] coordinates, and a list of [properties].
 * The data can be converted to a state with the appropriate [name][State.name],
 * [isInitial][State.isInitial] and [isFinal][State.isFinal] statuses,
 * [position][State.position], and [dynamic properties][State.properties].
 */
@IgnorableByCoverage
@Serializable
@SerialName("state")
data class StateData(
    override val id: Int,
    override val name: String,
    override val x: Double,
    override val y: Double,
    override val isInitial: Boolean = false,
    override val isFinal: Boolean = false,
    val properties: List<String> = emptyList()
) : AutomatonVertexData()

@IgnorableByCoverage
@Serializable
@SerialName("building-block")
data class BuildingBlockData(
    override val id: Int,
    override val name: String,
    override val x: Double,
    override val y: Double,
    override val isInitial: Boolean = false,
    override val isFinal: Boolean = false,
    val vertices: List<AutomatonVertexData>,
    val transitions: List<TransitionData>
) : AutomatonVertexData()

/**
 * Retrieves [data][AutomatonVertexData] for all [vertices][Automaton.vertices] os the automaton.
 */
fun Automaton.getVerticesData(): List<AutomatonVertexData> = vertices.mapIndexed { i, vertex ->
    when (vertex) {
        is State -> StateData(
            id = i,
            name = vertex.name,
            x = vertex.position.x,
            y = vertex.position.y,
            isInitial = vertex.isInitial,
            isFinal = vertex.isFinal,
            properties = vertex.readProperties()
        )
        is BuildingBlock -> {
            val automatonData = vertex.subAutomaton.getData()
            BuildingBlockData(
                id = i,
                name = vertex.name,
                x = vertex.position.x,
                y = vertex.position.y,
                isInitial = vertex.isInitial,
                isFinal = vertex.isFinal,
                vertices = automatonData.vertices,
                transitions = automatonData.transitions
            )
        }
    }

}
