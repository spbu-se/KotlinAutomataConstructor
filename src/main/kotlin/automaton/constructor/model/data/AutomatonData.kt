package automaton.constructor.model.data

import automaton.constructor.model.automaton.Automaton
import automaton.constructor.utils.IgnorableByCoverage
import javafx.geometry.Point2D
import kotlinx.serialization.Serializable

/**
 * The data of an [automaton][Automaton].
 *
 * It consists of a [base] type data, a list of [states] data, and a list of [transitions] data.
 */
@IgnorableByCoverage
@Serializable
data class AutomatonData(
    val base: AutomatonTypeData,
    val states: List<StateData>,
    val transitions: List<TransitionData>
)


/**
 * Retrieves the [data][AutomatonData] from the automaton.
 */
fun Automaton.getData() = AutomatonData(
    base = getTypeData(),
    states = getStatesData(),
    transitions = getTransitionsData()
)

/**
 * Creates an appropriate [automaton][Automaton] using this data.
 */
fun AutomatonData.createAutomaton() = base.createEmptyAutomaton().also { automaton ->
    val idToStateMap = states.associate {
        it.id to automaton.addState(it.name, Point2D(it.x, it.y)).apply {
            isInitial = it.isInitial
            isFinal = it.isFinal
            writeProperties(it.properties)
        }
    }
    for ((source, target, properties) in transitions) {
        automaton.addTransition(
            source = idToStateMap.getValue(source),
            target = idToStateMap.getValue(target)
        ).writeProperties(properties)
    }
    automaton.undoRedoManager.reset()
}
