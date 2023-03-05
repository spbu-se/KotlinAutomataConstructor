package automaton.constructor.model.data

import automaton.constructor.utils.MostlyGeneratedOrInline
import javafx.geometry.Point2D
import kotlinx.serialization.Serializable

@Serializable
@MostlyGeneratedOrInline
data class PointData(
    val x: Double,
    val y: Double
)

fun Point2D.toData() = PointData(x, y)
fun PointData.toPoint() = Point2D(x, y)
