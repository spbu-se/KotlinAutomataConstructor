package automaton.constructor.model.data

import automaton.constructor.model.automaton.Automaton
import automaton.constructor.utils.MostlyGeneratedOrInline
import javafx.geometry.Point2D
import kotlinx.serialization.Serializable

@MostlyGeneratedOrInline
@Serializable
data class AutomatonData(
    val base: AutomatonTypeData,
    val states: List<StateData>,
    val transitions: List<TransitionData>
)


fun Automaton.getData(): AutomatonData {
    val type = getTypeDataOrNull()
    val states = getStatesData()
    val transitions = getTransitionsData()
    requireNotNull(type) { "Cannot retrieve data from the ${this.typeName}" }
    return AutomatonData(type, states, transitions)
}

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
