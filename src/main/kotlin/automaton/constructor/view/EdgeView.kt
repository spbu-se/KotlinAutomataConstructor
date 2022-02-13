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
import kotlin.math.sqrt

class LoopEdgeRenderData(val state: State) : EdgeRenderData {
    companion object {
        private const val ARROW_START_OFFSET_Y = -0.5 * STATE_RADIUS
        private val ARROW_START_OFFSET_X =
            sqrt(STATE_RADIUS * STATE_RADIUS - ARROW_START_OFFSET_Y * ARROW_START_OFFSET_Y)
    }

    override val normXProperty = 0.0.toProperty()
    override val normX by normXProperty
    override val midPointXProperty = state.positionProperty.x
    override val midPointX by midPointXProperty
    override val normYProperty = (-1.0).toProperty()
    override val normY by normYProperty
    override val midPointYProperty = state.positionProperty.y - (2.0 * STATE_RADIUS)
    override val midPointY by midPointYProperty
    override val textAngleInDegreesProperty = 0.0.toProperty()
    override val children = listOf(
        Circle().apply {
            radius = STATE_RADIUS
            centerXProperty().bind(state.positionProperty.x)
            centerYProperty().bind(state.positionProperty.y - STATE_RADIUS)
            fill = Color.TRANSPARENT
            stroke = Color.BLACK
        },
        Line().apply {
            startXProperty().bind(state.positionProperty.x + ARROW_START_OFFSET_X)
            startYProperty().bind(state.positionProperty.y + ARROW_START_OFFSET_Y)
            endXProperty().bind(state.positionProperty.x + (ARROW_START_OFFSET_X + 25.0))
            endYProperty().bind(state.positionProperty.y + (ARROW_START_OFFSET_Y - 25.0))
        },
        Line().apply {
            startXProperty().bind(state.positionProperty.x + ARROW_START_OFFSET_X)
            startYProperty().bind(state.positionProperty.y + ARROW_START_OFFSET_Y)
            endXProperty().bind(state.positionProperty.x + (ARROW_START_OFFSET_X - 5.0))
            endYProperty().bind(state.positionProperty.y + (ARROW_START_OFFSET_Y - 35.0))
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
        text.rotateProperty().bind(edgeRenderData.textAngleInDegreesProperty)
        text.translateToCenter()
        text.xProperty()
            .bind(edgeRenderData.midPointXProperty.doubleBinding(edgeRenderData.normXProperty, indexProperty) {
                edgeRenderData.midPointX + 60 * (index + 0.5) * edgeRenderData.normX
            })
        text.yProperty()
            .bind(edgeRenderData.midPointYProperty.doubleBinding(edgeRenderData.normYProperty, indexProperty) {
                edgeRenderData.midPointY + 60 * (index + 0.5) * edgeRenderData.normY
            })
        transitionsGroup.add(text)
        transitionViews.add(this)
    }

    fun removeTransition(transition: Transition) {
        transitionViews.removeIf { transitionView ->
            val matches = transitionView.transition === transition
            if (matches) transitionsGroup.children.remove(transitionView.text)
            matches
        }
        transitionViews.forEachIndexed { i, transitionView -> transitionView.index = i }
    }
}
