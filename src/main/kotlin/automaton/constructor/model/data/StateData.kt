package automaton.constructor.model.data

import automaton.constructor.model.automaton.Automaton
import automaton.constructor.utils.IgnorableByCoverage
import kotlinx.serialization.Serializable

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
