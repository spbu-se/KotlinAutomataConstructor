package automaton.constructor.model.element

import javafx.beans.property.Property
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.SetChangeListener
import javafx.geometry.Point2D
import tornadofx.*

class AutomatonEdge(val source: AutomatonVertex, val target: AutomatonVertex) {
    val transitions = observableSetOf<Transition>()
    val routingProperty = SimpleObjectProperty<Routing>(null)
    var routing by routingProperty

    init {
        transitions.addListener(SetChangeListener {
            resetRouting()
        })
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
