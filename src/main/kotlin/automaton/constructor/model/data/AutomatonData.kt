package automaton.constructor.model.data

import automaton.constructor.model.automaton.Automaton
import automaton.constructor.utils.MostlyGeneratedOrInline
import javafx.geometry.Point2D
import kotlinx.serialization.Serializable

/**
 * The data of an [automaton][Automaton].
 *
 * It consists of a [base] type data, a list of [vertices] data, and a list of [transitions] data.
 */
@MostlyGeneratedOrInline
@Serializable
data class AutomatonData(
    val base: AutomatonTypeData,
    val vertices: Set<AutomatonVertexData>,
    val transitions: Set<TransitionData>,
    val edges: Set<AutomatonEdgeData> = emptySet()
)


/**
 * Retrieves the [data][AutomatonData] from the automaton.
 */
fun Automaton.getData(): AutomatonData {
    val vertexToIdMap = vertices.sortedWith(compareBy(
        { it.name },
        { it.allProperties.joinToString { prop -> prop.displayValue } },
        { it.position.x },
        { it.position.y }
    )).asSequence().withIndex().associate { (i, v) -> v to i }
    return AutomatonData(
        base = getTypeData(),
        vertices = getVerticesData(vertexToIdMap),
        transitions = getTransitionsData(vertexToIdMap),
        edges = getEdgesData(vertexToIdMap)
    )
}

/**
 * Creates an appropriate [automaton][Automaton] using this data.
 */
fun AutomatonData.createAutomaton(): Automaton = base.createEmptyAutomaton().also { automaton ->
    automaton.addContent(vertices, transitions, edges)
}

fun Automaton.addContent(
    verticesData: Set<AutomatonVertexData>,
    transitionsData: Set<TransitionData>,
    edgesData: Set<AutomatonEdgeData>
) {
    val idToVertexMap = verticesData.associate {
        it.id to when (it) {
            is StateData -> addState(it.name, Point2D(it.x, it.y)).apply { writeProperties(it.properties) }
            is BuildingBlockData -> addBuildingBlock(
                createSubAutomaton().apply { addContent(it.vertices, it.transitions, it.edges) },
                it.name,
                Point2D(it.x, it.y)
            )
        }.apply {
            isInitial = it.isInitial
            isFinal = it.isFinal
            requiresLayout = it.requiresLayout
        }
    }
    for ((source, target, properties, position) in transitionsData) {
        addTransition(
            source = idToVertexMap.getValue(source),
            target = idToVertexMap.getValue(target)
        ).apply {
            this.position = position?.toPoint()
            writeProperties(properties)
        }
    }
    for ((source, target, routingData) in edgesData)
        edges[idToVertexMap.getValue(source) to idToVertexMap.getValue(target)]?.routing = routingData?.toRouting()
    undoRedoManager.reset()
}
