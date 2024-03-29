package automaton.constructor.model.element

import javafx.beans.property.Property
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.SetChangeListener
import javafx.geometry.Point2D
import tornadofx.getValue
import tornadofx.observableSetOf
import tornadofx.onChange
import tornadofx.setValue

class AutomatonEdge(val source: AutomatonVertex, val target: AutomatonVertex) {
    companion object {
        private const val MAX_END_POINT_DEVIATION = 2 * AutomatonVertex.RADIUS
    }

    val transitions = observableSetOf<Transition>()
    val routingProperty = SimpleObjectProperty<Routing>(null)
    var routing by routingProperty

    init {
        transitions.addListener(SetChangeListener {
            resetRouting()
            if (it.wasAdded()) it.elementAdded.resetPosition()
        })
        routingProperty.onChange {
            if (!isRoutingValid()) resetRouting()
        }
    }

    private fun isRoutingValid() = when (val routing = routing) {
        null -> true
        is PiecewiseCubicSpline ->
            routing.splinePoints.first().distance(source.position) < MAX_END_POINT_DEVIATION &&
                routing.splinePoints.last().distance(target.position) < MAX_END_POINT_DEVIATION
    }

    fun resetRouting() {
        if (routing != null) {
            routing = null
            transitions.forEach { it.resetPosition() }
        }
    }

    val undoRedoProperties = listOf<Property<*>>(routingProperty)

    sealed interface Routing
    class PiecewiseCubicSpline(val splinePoints: List<Point2D>) : Routing {
        val cubicCurves get() =
            (List(2 - (splinePoints.size + 1) % 3) { splinePoints.first() } + splinePoints.asSequence())
            .windowed(size=4, step=3)
    }
}
