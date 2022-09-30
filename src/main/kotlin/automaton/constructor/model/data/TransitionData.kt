package automaton.constructor.model.data

import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.element.Transition
import automaton.constructor.utils.IgnorableByCoverage
import kotlinx.serialization.Serializable

/**
 * The data of a [transition][Transition].
 *
 * It consists of a [source] index, a [target] index, and a list of [properties].
 * The data can be converted to a transition with the appropriate [source state][Transition.source],
 * [target state][Transition.target], and [dynamic properties][Transition.properties].
 */
@IgnorableByCoverage
@Serializable
data class TransitionData(
    val source: Int,
    val target: Int,
    val properties: List<String>
)

/**
 * Retrieves all [transition data][TransitionData] from the automaton.
 */
fun Automaton.getTransitionsData(): List<TransitionData> {
    val stateToIdMap = vertices.asSequence().withIndex().associate { (i, v) -> v to i }
    return transitions.map { transition ->
        TransitionData(
            source = stateToIdMap.getValue(transition.source),
            target = stateToIdMap.getValue(transition.target),
            properties = transition.readProperties()
        )
    }
}
