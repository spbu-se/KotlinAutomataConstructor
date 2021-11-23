package automaton.constructor.view

import automaton.constructor.model.State
import automaton.constructor.model.transition.Transition
import automaton.constructor.utils.*
import javafx.beans.value.ObservableBooleanValue
import javafx.beans.value.ObservableDoubleValue
import javafx.collections.ObservableList
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.shape.Line
import tornadofx.*
import java.lang.Math.toDegrees
import kotlin.math.atan

class LoopEdgeRenderData(val state: State) : EdgeRenderData {
    companion object {
        val LOOP_NORM: Vector2D = Vector2D(1.0, -1.0).normalize()
    }

    override val normXProperty = LOOP_NORM.x.toProperty()
    override val normX by normXProperty
    override val midPointXProperty = state.positionProperty.doubleBinding {
        state.position.x + (normX + 1) * STATE_RADIUS
    }
    override val midPointX by midPointXProperty
    override val normYProperty = LOOP_NORM.y.toProperty()
    override val normY by normYProperty
    override val midPointYProperty = state.positionProperty.doubleBinding {
        state.position.y + (normY - 1) * STATE_RADIUS
    }
    override val midPointY by midPointYProperty
    override val textAngleInDegreesProperty = 45.0.toProperty()
    override val children = listOf(
        Circle().apply {
            radius = STATE_RADIUS
            centerXProperty().bind(state.positionProperty.x + STATE_RADIUS)
            centerYProperty().bind(state.positionProperty.y - STATE_RADIUS)
            fill = Color.TRANSPARENT
            stroke = Color.BLACK
        },
        Line().apply {
            startXProperty().bind(state.positionProperty.x + STATE_RADIUS)
            startYProperty().bind(state.positionProperty.y)
            endXProperty().bind(state.positionProperty.x + STATE_RADIUS + 35.0)
            endYProperty().bind(state.positionProperty.y + 5.0)
        },
        Line().apply {
            startXProperty().bind(state.positionProperty.x + STATE_RADIUS)
            startYProperty().bind(state.positionProperty.y)
            endXProperty().bind(state.positionProperty.x + STATE_RADIUS + 30.0)
            endYProperty().bind(state.positionProperty.y - 20.0)
        }
    )
}

class NonLoopEdgeRenderData(
    val source: State,
    val target: State,
    val hasOppositeProperty: ObservableBooleanValue
) : EdgeRenderData {
    override val normXProperty = source.positionProperty.doubleBinding(target.positionProperty) {
        (target.position.y - source.position.y) / source.position.distance(target.position)
    }
    override val normX by normXProperty
    override val midPointXProperty =
        normXProperty.doubleBinding(hasOppositeProperty, source.positionProperty, target.positionProperty) {
            if (hasOppositeProperty.value) (source.position.x + target.position.x) / 2 + 80.0 * normX
            else (source.position.x + target.position.x) / 2
        }
    override val midPointX by midPointXProperty
    override val normYProperty = source.positionProperty.doubleBinding(target.positionProperty) {
        (source.position.x - target.position.x) / source.position.distance(target.position)
    }
    override val normY by normYProperty
    override val midPointYProperty =
        normYProperty.doubleBinding(hasOppositeProperty, source.positionProperty, target.positionProperty) {
            if (hasOppositeProperty.value) (source.position.y + target.position.y) / 2 + 80.0 * normY
            else (source.position.y + target.position.y) / 2
        }
    override val midPointY by midPointYProperty
    override val textAngleInDegreesProperty = source.positionProperty.doubleBinding(target.positionProperty) {
        toDegrees(atan((target.position.y - source.position.y) / (target.position.x - source.position.x)))
            .toInt().toDouble().roundTo(3)
    }

    override val children = listOf(
        Line().apply {
            startXProperty().bind(source.positionProperty.x)
            startYProperty().bind(source.positionProperty.y)
            endXProperty().bind(midPointXProperty)
            endYProperty().bind(midPointYProperty)
        },
        Arrow(Line(), 50.0, 20.0).apply {
            line.startXProperty().bind(midPointXProperty)
            line.startYProperty().bind(midPointYProperty)
            val endProperty = target.positionProperty.nonNullObjectBinding(midPointXProperty, midPointYProperty) {
                target.position - STATE_RADIUS * Vector2D(
                    target.position.x - midPointX,
                    target.position.y - midPointY
                ).normalize()
            }
            line.endXProperty().bind(endProperty.x)
            line.endYProperty().bind(endProperty.y)
        })
}

interface EdgeRenderData {
    val normXProperty: ObservableDoubleValue
    val normX: Double
    val midPointXProperty: ObservableDoubleValue
    val midPointX: Double
    val normYProperty: ObservableDoubleValue
    val normY: Double
    val midPointYProperty: ObservableDoubleValue
    val midPointY: Double
    val textAngleInDegreesProperty: ObservableDoubleValue

    val children: List<Node>
}

class EdgeView(val source: State, val target: State) : Group() {
    val transitionViews: ObservableList<TransitionView> = observableListOf()
    val oppositeEdgeProperty = objectProperty<EdgeView?>(null)
    var oppositeEdge by oppositeEdgeProperty
    private val edgeRenderData =
        if (source === target) LoopEdgeRenderData(source)
        else NonLoopEdgeRenderData(source, target, oppositeEdgeProperty.isNotNull)

    init {
        edgeRenderData.children.forEach { add(it) }
    }

    val transitionsGroup = group()

    fun addTransition(transition: Transition) = TransitionView(transition, transitionViews.size).apply {
        rotateProperty().bind(edgeRenderData.textAngleInDegreesProperty)
        translateToCenter()
        xProperty().bind(edgeRenderData.midPointXProperty.doubleBinding(edgeRenderData.normXProperty, indexProperty) {
            edgeRenderData.midPointX + 60 * (index + 0.5) * edgeRenderData.normX
        })
        yProperty().bind(edgeRenderData.midPointYProperty.doubleBinding(edgeRenderData.normYProperty, indexProperty) {
            edgeRenderData.midPointY + 60 * (index + 0.5) * edgeRenderData.normY
        })
        transitionsGroup.add(this)
        transitionViews.add(this)
    }

    fun removeTransition(transition: Transition) {
        transitionViews.removeIf { transitionView ->
            val matches = transitionView.transition === transition
            if (matches) transitionsGroup.children.remove(transitionView)
            matches
        }
        transitionViews.forEachIndexed { i, transitionView -> transitionView.index = i }
    }
}
