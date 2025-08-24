package automaton.constructor.model.data

import automaton.constructor.model.automaton.Automaton
import automaton.constructor.utils.MostlyGeneratedOrInline
import javafx.geometry.Point2D
import kotlinx.serialization.Serializable
import java.util.*

/**
 * The data of an [automaton][Automaton].
 *
 * It consists of a [base] type data, a list of [vertices] data, list of [transitions] data and a list of [edges] data.
 *
 * [edges] data may skip non-routed edges for backward compatability.
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
fun Automaton.getData(): AutomatonData = getDataInternal(Collections.newSetFromMap(IdentityHashMap()))

internal fun Automaton.getDataInternal(
    visited: MutableSet<Automaton>,
): AutomatonData {
    if (!visited.add(this)) {
        return AutomatonData(
            base = getTypeData(),
            vertices = emptySet(),
            transitions = emptySet(),
            edges = emptySet()
        )
    }
    val vertexToIdMap = vertices.sortedWith(
        compareBy(
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
    edgesData: Set<AutomatonEdgeData>,
    recursiveSubs: MutableMap<String, Automaton>? = null
) {
    val subs = recursiveSubs ?: mutableMapOf()
    val orderedVerticesData: List<AutomatonVertexData> = run {
        val (boxes, others) = verticesData.partition { it is RecursiveAutomatonBoxData }
        val (withContent, withoutContent) = boxes.partition { box ->
            box is RecursiveAutomatonBoxData && (box.vertices.isNotEmpty() || box.transitions.isNotEmpty())
        }
        withContent + withoutContent + others
    }
    val idToVertexMap = orderedVerticesData.associate { vData ->
        vData.id to when (vData) {
            is StateData -> addState(vData.name, Point2D(vData.x, vData.y)).apply { writeProperties(vData.properties) }
            is BuildingBlockData -> addBuildingBlock(
                createEmptyAutomatonOfSameType().apply { addContent(vData.vertices, vData.transitions, vData.edges) },
                vData.name,
                Point2D(vData.x, vData.y)
            )

            is RecursiveAutomatonBoxData -> {
                val subAutomaton = if (vData.vertices.isNotEmpty() || vData.transitions.isNotEmpty()) {
                    subs[vData.name] ?: createEmptyAutomatonOfSameType().apply {
                        addContent(vData.vertices, vData.transitions, vData.edges, subs)
                    }.also { sub -> subs.putIfAbsent(vData.name, sub) }
                } else {
                    subs[vData.name] ?: this
                }
                addRecursiveAutomatonBox(
                    subAutomaton = subAutomaton,
                    name = vData.name,
                    position = Point2D(vData.x, vData.y),
                    bindName = false,
                    registerSubManager = true,
                    visibleInParent = true
                )
            }
        }.apply {
            isInitial = vData.isInitial
            isFinal = vData.isFinal
            requiresLayout = vData.requiresLayout
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
