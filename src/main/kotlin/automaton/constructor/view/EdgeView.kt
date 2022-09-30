package automaton.constructor.view

import automaton.constructor.model.element.AutomatonVertex.Companion.RADIUS
import automaton.constructor.model.element.Transition
import automaton.constructor.utils.*
import automaton.constructor.view.AutomatonVertexView.ShapeType.CIRCLE
import automaton.constructor.view.AutomatonVertexView.ShapeType.SQUARE
import javafx.beans.value.ObservableBooleanValue
import javafx.beans.value.ObservableDoubleValue
import javafx.beans.value.ObservableValue
import javafx.collections.ObservableList
import javafx.geometry.Point2D
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.shape.Line
import tornadofx.*
import java.lang.Math.toDegrees
import kotlin.math.abs
import kotlin.math.atan
import kotlin.math.max
import kotlin.math.sqrt

enum class TransitionLabelPosition {
    COUNTER_CLOCKWISE,
    ABOVE
}

class LoopEdgeRenderData(
    vertex: AutomatonVertexView,
    val center: ObservableValue<Point2D>
) : EdgeRenderData {
    private interface Constants {
        val loopRadius: Double
        val arrowStartOffsetX: Double
        val arrowStartOffsetY: Double
        val leftArrowLineDX: Double
        val leftArrowLineDY: Double
        val rightArrowLineDX: Double
        val rightArrowLineDY: Double
    }

    private object CircleConstants : Constants {
        override val loopRadius = RADIUS
        override val arrowStartOffsetY = -0.5 * RADIUS
        override val arrowStartOffsetX =
            sqrt(RADIUS * RADIUS - arrowStartOffsetY * arrowStartOffsetY)
        override val leftArrowLineDX = -RADIUS * 0.1
        override val leftArrowLineDY = -RADIUS * 0.7
        override val rightArrowLineDX = RADIUS * 0.5
        override val rightArrowLineDY = -RADIUS * 0.5
    }

    companion object SquareConstants : Constants {
        override val loopRadius = RADIUS * 0.8
        override val arrowStartOffsetY = -RADIUS
        override val arrowStartOffsetX = loopRadius
        override val leftArrowLineDX = -RADIUS * 0.4
        override val leftArrowLineDY = -RADIUS * 0.4
        override val rightArrowLineDX = RADIUS * 0.3
        override val rightArrowLineDY = -RADIUS * 0.5
    }

    private val constants = when (vertex.shapeType) {
        CIRCLE -> CircleConstants
        SQUARE -> SquareConstants
    }
    override val normXProperty = 0.0.toProperty()
    override val normX by normXProperty
    override val midPointXProperty = center.x
    override val midPointX by midPointXProperty
    override val normYProperty = (-1.0).toProperty()
    override val normY by normYProperty
    override val midPointYProperty = center.y - RADIUS - constants.loopRadius
    override val midPointY by midPointYProperty
    override val textAngleInDegreesProperty = 0.0.toProperty()
    override val children = with(constants) {
        listOf(
            Circle().apply {
                radius = loopRadius
                centerXProperty().bind(center.x)
                centerYProperty().bind(center.y - RADIUS)
                fill = Color.TRANSPARENT
                stroke = Color.BLACK
            },
            Line().apply {
                startXProperty().bind(center.x + arrowStartOffsetX)
                startYProperty().bind(center.y + arrowStartOffsetY)
                endXProperty().bind(center.x + (arrowStartOffsetX + leftArrowLineDX))
                endYProperty().bind(center.y + (arrowStartOffsetY + leftArrowLineDY))
            },
            Line().apply {
                startXProperty().bind(center.x + arrowStartOffsetX)
                startYProperty().bind(center.y + arrowStartOffsetY)
                endXProperty().bind(center.x + (arrowStartOffsetX + rightArrowLineDX))
                endYProperty().bind(center.y + (arrowStartOffsetY + rightArrowLineDY))
            }
        )
    }
}

class NonLoopEdgeRenderData(
    source: AutomatonVertexView,
    target: AutomatonVertexView,
    val sourceCenter: ObservableValue<Point2D>,
    val targetCenter: ObservableValue<Point2D>,
    val hasOppositeProperty: ObservableBooleanValue,
    labelPosition: TransitionLabelPosition
) : EdgeRenderData {
    private val isLabelMirroredProperty: ObservableValue<Boolean> = when (labelPosition) {
        TransitionLabelPosition.ABOVE -> sourceCenter.x.greaterThan(targetCenter.x)
        TransitionLabelPosition.COUNTER_CLOCKWISE -> false.toProperty()
    }
    private val isLabelMirrored: Boolean by isLabelMirroredProperty
    private val normFactor get() = if (isLabelMirrored) -1.0 else 1.0
    override val normXProperty = sourceCenter.doubleBinding(targetCenter, isLabelMirroredProperty) {
        normFactor * (targetCenter.value.y - sourceCenter.value.y) / sourceCenter.value.distance(targetCenter.value)
    }
    override val normX by normXProperty
    override val midPointXProperty =
        normXProperty.doubleBinding(hasOppositeProperty, sourceCenter, targetCenter) {
            if (hasOppositeProperty.value) (sourceCenter.value.x + targetCenter.value.x) / 2 + 80.0 * normX
            else (sourceCenter.value.x + targetCenter.value.x) / 2
        }
    override val midPointX by midPointXProperty
    override val normYProperty = sourceCenter.doubleBinding(targetCenter, isLabelMirroredProperty) {
        normFactor * (sourceCenter.value.x - targetCenter.value.x) / sourceCenter.value.distance(targetCenter.value)
    }
    override val normY by normYProperty
    override val midPointYProperty =
        normYProperty.doubleBinding(hasOppositeProperty, sourceCenter, targetCenter) {
            if (hasOppositeProperty.value) (sourceCenter.value.y + targetCenter.value.y) / 2 + 80.0 * normY
            else (sourceCenter.value.y + targetCenter.value.y) / 2
        }
    override val midPointY by midPointYProperty
    override val textAngleInDegreesProperty = sourceCenter.doubleBinding(targetCenter) {
        toDegrees(atan((targetCenter.value.y - sourceCenter.value.y) / (targetCenter.value.x - sourceCenter.value.x)))
            .toInt().toDouble().roundTo(3)
    }

    override val children = listOf(
        Line().apply {
            startXProperty().bind(sourceCenter.x)
            startYProperty().bind(sourceCenter.y)
            endXProperty().bind(midPointXProperty)
            endYProperty().bind(midPointYProperty)
        },
        Arrow(Line(), 50.0, 20.0).apply {
            line.startXProperty().bind(midPointXProperty)
            line.startYProperty().bind(midPointYProperty)
            val endProperty = targetCenter.nonNullObjectBinding(midPointXProperty, midPointYProperty) {
                val v = Vector2D(
                    targetCenter.value.x - midPointX,
                    targetCenter.value.y - midPointY
                )
                if (v == Vector2D.ZERO) targetCenter.value
                else targetCenter.value - RADIUS * when (target.shapeType) {
                    CIRCLE -> v.normalize()
                    SQUARE -> v / max(abs(v.x), abs(v.y))
                }
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

class EdgeView(
    source: AutomatonVertexView,
    target: AutomatonVertexView,
    sourceCenter: ObservableValue<Point2D> = source.positionProperty,
    targetCenter: ObservableValue<Point2D> = target.positionProperty,
    labelPosition: TransitionLabelPosition = TransitionLabelPosition.COUNTER_CLOCKWISE
) : Group() {
    val transitionViews: ObservableList<TransitionView> = observableListOf()
    val oppositeEdgeProperty = objectProperty<EdgeView?>(null)
    var oppositeEdge by oppositeEdgeProperty
    private val edgeRenderData =
        if (sourceCenter === targetCenter) LoopEdgeRenderData(source, sourceCenter)
        else NonLoopEdgeRenderData(
            source, target,
            sourceCenter, targetCenter,
            oppositeEdgeProperty.isNotNull,
            labelPosition
        )

    init {
        edgeRenderData.children.forEach { add(it) }
    }

    val transitionsGroup = group()

    fun addTransition(transition: Transition) = TransitionView(transition, transitionViews.size).apply {
        rotateProperty().bind(edgeRenderData.textAngleInDegreesProperty)
        xProperty
            .bind(edgeRenderData.midPointXProperty.doubleBinding(edgeRenderData.normXProperty, indexProperty) {
                edgeRenderData.midPointX + 60 * (index + 0.5) * edgeRenderData.normX
            })
        yProperty
            .bind(edgeRenderData.midPointYProperty.doubleBinding(edgeRenderData.normYProperty, indexProperty) {
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
