package automaton.constructor.model.data

import automaton.constructor.model.automaton.Automaton
import automaton.constructor.utils.IgnorableByCoverage
import kotlinx.serialization.Serializable

@IgnorableByCoverage
@Serializable
data class TransitionData(
    val source: Int,
    val target: Int,
    val properties: List<String>
)

fun Automaton.getTransitionsData(): List<TransitionData> {
    val stateToIdMap = states.asSequence().withIndex().associate { (i, v) -> v to i }
    return transitions.map { transition ->
        TransitionData(
            source = stateToIdMap.getValue(transition.source),
            target = stateToIdMap.getValue(transition.target),
            properties = transition.readProperties()
        )
    }
}
