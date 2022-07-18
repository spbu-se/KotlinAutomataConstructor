package automaton.constructor.model.data

import automaton.constructor.model.State
import automaton.constructor.model.automaton.Automaton
import automaton.constructor.utils.IgnorableByCoverage
import kotlinx.serialization.Serializable

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
data class StateData(
    val id: Int,
    val name: String,
    val isInitial: Boolean = false,
    val isFinal: Boolean = false,
    val x: Double,
    val y: Double,
    val properties: List<String> = emptyList()
)

/**
 * Retrieves all [state data][StateData] from the automaton.
 */
fun Automaton.getStatesData(): List<StateData> = states.mapIndexed { i, state ->
    StateData(
        id = i,
        name = state.name,
        isInitial = state.isInitial,
        isFinal = state.isFinal,
        x = state.position.x,
        y = state.position.y,
        properties = state.readProperties()
    )
}
