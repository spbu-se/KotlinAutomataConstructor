package automaton.constructor.model.data

import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.element.AutomatonEdge
import automaton.constructor.model.element.AutomatonEdge.PiecewiseCubicSpline
import automaton.constructor.model.element.AutomatonVertex
import automaton.constructor.utils.MostlyGeneratedOrInline
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AutomatonEdgeData(
    val source: Int,
    val target: Int,
    val routing: AutomatonEdgeRoutingData?
)

@Serializable
sealed class AutomatonEdgeRoutingData {
    companion object
}

@Serializable
@SerialName("piecewise-cubic-spline")
@MostlyGeneratedOrInline
data class PiecewiseCubicSplineData(
    val splinePoints: List<PointData>
) : AutomatonEdgeRoutingData()

fun Automaton.getEdgesData(vertexToIdMap: Map<AutomatonVertex, Int>): Set<AutomatonEdgeData> =
    edges.values.filter { it.routing != null }.map { edge ->
        AutomatonEdgeData(
            source = vertexToIdMap.getValue(edge.source),
            target = vertexToIdMap.getValue(edge.target),
            routing = edge.routing?.toData()
        )
    }.toSet()

fun AutomatonEdge.Routing.toData() = when (this) {
    is PiecewiseCubicSpline -> PiecewiseCubicSplineData(splinePoints.map { it.toData() })
}

fun AutomatonEdgeRoutingData.toRouting() = when (this) {
    is PiecewiseCubicSplineData -> PiecewiseCubicSpline(splinePoints.map { it.toPoint() })
}

