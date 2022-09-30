package automaton.constructor.model.data

import automaton.constructor.model.automaton.Automaton
import automaton.constructor.utils.IgnorableByCoverage
import javafx.geometry.Point2D
import kotlinx.serialization.Serializable

/**
 * The data of an [automaton][Automaton].
 *
 * It consists of a [base] type data, a list of [vertices] data, and a list of [transitions] data.
 */
@IgnorableByCoverage
@Serializable
data class AutomatonData(
    val base: AutomatonTypeData,
    val vertices: List<AutomatonVertexData>,
    val transitions: List<TransitionData>
)


/**
 * Retrieves the [data][AutomatonData] from the automaton.
 */
fun Automaton.getData() = AutomatonData(
    base = getTypeData(),
    vertices = getVerticesData(),
    transitions = getTransitionsData()
)

/**
 * Creates an appropriate [automaton][Automaton] using this data.
 */
fun AutomatonData.createAutomaton(): Automaton = base.createEmptyAutomaton().also { automaton ->
    automaton.addContent(vertices, transitions)
}

private fun Automaton.addContent(verticesData: List<AutomatonVertexData>, transitionsData: List<TransitionData>) {
    val idToStateMap = verticesData.associate {
        it.id to when (it) {
            is StateData -> addState(it.name, Point2D(it.x, it.y)).apply { writeProperties(it.properties) }
            is BuildingBlockData -> addBuildingBlock(
                createSubAutomaton().apply { addContent(it.vertices, it.transitions) },
                it.name,
                Point2D(it.x, it.y)
            )
        }.apply {
            isInitial = it.isInitial
            isFinal = it.isFinal
        }
    }
    for ((source, target, properties) in transitionsData) {
        addTransition(
            source = idToStateMap.getValue(source),
            target = idToStateMap.getValue(target)
        ).writeProperties(properties)
    }
    undoRedoManager.reset()
}
