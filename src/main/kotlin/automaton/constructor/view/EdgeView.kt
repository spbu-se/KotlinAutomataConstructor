package automaton.constructor.view

import automaton.constructor.model.State
import automaton.constructor.model.transition.Transition
import automaton.constructor.utils.*
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
import kotlin.math.atan
import kotlin.math.sqrt

enum class TransitionLabelPosition {
    COUNTER_CLOCKWISE,
    ABOVE
}

class LoopEdgeRenderData(val center: ObservableValue<Point2D>) : EdgeRenderData {
    companion object {
        private const val ARROW_START_OFFSET_Y = -0.5 * StateView.RADIUS
        private val ARROW_START_OFFSET_X =
            sqrt(StateView.RADIUS * StateView.RADIUS - ARROW_START_OFFSET_Y * ARROW_START_OFFSET_Y)
    }

    override val normXProperty = 0.0.toProperty()
    override val normX by normXProperty
    override val midPointXProperty = center.x
    override val midPointX by midPointXProperty
    override val normYProperty = (-1.0).toProperty()
    override val normY by normYProperty
    override val midPointYProperty = center.y - (2.0 * StateView.RADIUS)
    override val midPointY by midPointYProperty
    override val textAngleInDegreesProperty = 0.0.toProperty()
    override val children = listOf(
        Circle().apply {
            radius = StateView.RADIUS
            centerXProperty().bind(center.x)
            centerYProperty().bind(center.y - StateView.RADIUS)
            fill = Color.TRANSPARENT
            stroke = Color.BLACK
        },
        Line().apply {
            startXProperty().bind(center.x + ARROW_START_OFFSET_X)
            startYProperty().bind(center.y + ARROW_START_OFFSET_Y)
            endXProperty().bind(center.x + (ARROW_START_OFFSET_X + 25.0))
            endYProperty().bind(center.y + (ARROW_START_OFFSET_Y - 25.0))
        },
        Line().apply {
            startXProperty().bind(center.x + ARROW_START_OFFSET_X)
            startYProperty().bind(center.y + ARROW_START_OFFSET_Y)
            endXProperty().bind(center.x + (ARROW_START_OFFSET_X - 5.0))
            endYProperty().bind(center.y + (ARROW_START_OFFSET_Y - 35.0))
        }
    )
}

class NonLoopEdgeRenderData(
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
                targetCenter.value - StateView.RADIUS * Vector2D(
                    targetCenter.value.x - midPointX,
                    targetCenter.value.y - midPointY
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

class EdgeView(
    sourceCenter: ObservableValue<Point2D>,
    targetCenter: ObservableValue<Point2D>,
    labelPosition: TransitionLabelPosition = TransitionLabelPosition.COUNTER_CLOCKWISE
) : Group() {
    constructor(source: State, target: State) : this(source.positionProperty, target.positionProperty)

    val transitionViews: ObservableList<TransitionView> = observableListOf()
    val oppositeEdgeProperty = objectProperty<EdgeView?>(null)
    var oppositeEdge by oppositeEdgeProperty
    private val edgeRenderData =
        if (sourceCenter === targetCenter) LoopEdgeRenderData(sourceCenter)
        else NonLoopEdgeRenderData(sourceCenter, targetCenter, oppositeEdgeProperty.isNotNull, labelPosition)

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
