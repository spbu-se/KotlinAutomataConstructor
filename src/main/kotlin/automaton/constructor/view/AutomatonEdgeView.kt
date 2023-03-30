package automaton.constructor.view

import automaton.constructor.model.element.AutomatonEdge
import automaton.constructor.model.element.AutomatonVertex.Companion.RADIUS
import automaton.constructor.model.element.Transition
import automaton.constructor.utils.*
import automaton.constructor.view.AutomatonVertexView.ShapeType
import automaton.constructor.view.AutomatonVertexView.ShapeType.CIRCLE
import automaton.constructor.view.AutomatonVertexView.ShapeType.SQUARE
import javafx.beans.value.ObservableBooleanValue
import javafx.beans.value.ObservableDoubleValue
import javafx.beans.value.ObservableValue
import javafx.collections.ObservableList
import javafx.collections.SetChangeListener
import javafx.geometry.Point2D
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.paint.Color
import javafx.scene.shape.Line
import tornadofx.*
import java.lang.Math.toDegrees
import kotlin.math.atan
import kotlin.math.sqrt

enum class TransitionLabelPosition {
    COUNTER_CLOCKWISE,
    ABOVE
}

private const val ARROW_LENGTH = 25.0
private const val ARROW_WIDTH = 10.0

class LoopEdgeRenderer(
    val vertexShape: ShapeType,
    val center: ObservableValue<Point2D>
) : EdgeRenderer {
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
        override val leftArrowLineDX = -RADIUS * 0.05
        override val leftArrowLineDY = -RADIUS * 0.35
        override val rightArrowLineDX = RADIUS * 0.25
        override val rightArrowLineDY = -RADIUS * 0.25
    }

    companion object SquareConstants : Constants {
        override val loopRadius = RADIUS * 0.8
        override val arrowStartOffsetY = -RADIUS
        override val arrowStartOffsetX = loopRadius
        override val leftArrowLineDX = -RADIUS * 0.2
        override val leftArrowLineDY = -RADIUS * 0.2
        override val rightArrowLineDX = RADIUS * 0.15
        override val rightArrowLineDY = -RADIUS * 0.25
    }

    private val constants = when (vertexShape) {
        CIRCLE -> CircleConstants
        SQUARE -> SquareConstants
    }
    override val sourceShapeType: ShapeType get() = vertexShape
    override val targetShapeType: ShapeType get() = vertexShape
    override val sourceCenter: ObservableValue<Point2D> get() = center
    override val targetCenter: ObservableValue<Point2D> get() = center
    override val normXProperty = 0.0.toProperty()
    override val normX by normXProperty
    override val midPointXProperty = center.x
    override val midPointX by midPointXProperty
    override val normYProperty = (-1.0).toProperty()
    override val normY by normYProperty
    override val midPointYProperty = center.y - RADIUS - constants.loopRadius
    override val midPointY by midPointYProperty
    override val textAngleInDegreesProperty = 0.0.toProperty()
    override val edgeNode = with(constants) {
        Group().apply {
            circle {
                radius = loopRadius
                centerXProperty().bind(center.x)
                centerYProperty().bind(center.y - RADIUS)
                fill = Color.TRANSPARENT
                stroke = Color.BLACK
            }
            line {
                startXProperty().bind(center.x + arrowStartOffsetX)
                startYProperty().bind(center.y + arrowStartOffsetY)
                endXProperty().bind(center.x + (arrowStartOffsetX + leftArrowLineDX))
                endYProperty().bind(center.y + (arrowStartOffsetY + leftArrowLineDY))
            }
            line {
                startXProperty().bind(center.x + arrowStartOffsetX)
                startYProperty().bind(center.y + arrowStartOffsetY)
                endXProperty().bind(center.x + (arrowStartOffsetX + rightArrowLineDX))
                endYProperty().bind(center.y + (arrowStartOffsetY + rightArrowLineDY))
            }
        }
    }
}

class NonLoopEdgeRenderer(
    override val sourceShapeType: ShapeType,
    override val targetShapeType: ShapeType,
    override val sourceCenter: ObservableValue<Point2D>,
    override val targetCenter: ObservableValue<Point2D>,
    val hasOppositeProperty: ObservableBooleanValue,
    labelPosition: TransitionLabelPosition
) : EdgeRenderer {
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

    override val edgeNode = Group().apply {
        line {
            startXProperty().bind(sourceCenter.x)
            startYProperty().bind(sourceCenter.y)
            endXProperty().bind(midPointXProperty)
            endYProperty().bind(midPointYProperty)
        }
        add(Arrow(Line(), ARROW_LENGTH, ARROW_WIDTH).apply {
            line.startXProperty().bind(midPointXProperty)
            line.startYProperty().bind(midPointYProperty)
            val endProperty = targetCenter.nonNullObjectBinding(midPointXProperty, midPointYProperty) {
                targetShapeType.project(targetCenter.value, Point2D(midPointX, midPointY))
            }
            line.endXProperty().bind(endProperty.x)
            line.endYProperty().bind(endProperty.y)
        })
    }
}

class SplineAwareEdgeRenderer(val wrapped: EdgeRenderer, val edge: AutomatonEdge?) : EdgeRenderer by wrapped {
    override val edgeNode: Group = Group()

    init {
        updateEdgeNode()
        edge?.routingProperty?.onChange { updateEdgeNode() }
    }

    private fun updateEdgeNode() {
        edgeNode.children.clear()
        when (val routing = edge?.routing) {
            null -> edgeNode.add(wrapped.edgeNode)
            is AutomatonEdge.PiecewiseCubicSpline -> {
                routing.cubicCurves.forEachIndexed { i, curve ->
                    val start =
                        if (i == 0) wrapped.sourceShapeType.project(wrapped.sourceCenter.value, curve[0])
                        else curve[0]
                    val end =
                        if (i == routing.cubicCurves.lastIndex) wrapped.targetShapeType.project(wrapped.targetCenter.value, curve[3])
                        else curve[3]
                    edgeNode.cubiccurve(
                        start.x,
                        start.y,
                        curve[1].x,
                        curve[1].y,
                        curve[2].x,
                        curve[2].y,
                        end.x,
                        end.y
                    ) {
                        fill = null
                        stroke = Color.BLACK
                        strokeWidth = 1.0
                    }
                }
                edgeNode.add(Arrow(routing.cubicCurves.last().let { curve ->
                    val end = wrapped.targetShapeType.project(wrapped.targetCenter.value, curve[3])
                    Line(curve[2].x, curve[2].y, end.x, end.y)
                }, ARROW_LENGTH, ARROW_WIDTH, showLine = false))
            }
        }
    }
}

interface EdgeRenderer {
    val sourceShapeType: ShapeType
    val targetShapeType: ShapeType
    val sourceCenter: ObservableValue<Point2D>
    val targetCenter: ObservableValue<Point2D>
    val normXProperty: ObservableDoubleValue
    val normX: Double
    val midPointXProperty: ObservableDoubleValue
    val midPointX: Double
    val normYProperty: ObservableDoubleValue
    val normY: Double
    val midPointYProperty: ObservableDoubleValue
    val midPointY: Double
    val textAngleInDegreesProperty: ObservableDoubleValue

    val edgeNode: Node
}

class AutomatonEdgeView(
    edge: AutomatonEdge?,
    sourceShapeType: ShapeType,
    targetShapeType: ShapeType,
    sourceCenter: ObservableValue<Point2D>,
    targetCenter: ObservableValue<Point2D>,
    labelPosition: TransitionLabelPosition = TransitionLabelPosition.COUNTER_CLOCKWISE
) : Group() {
    constructor(
        edge: AutomatonEdge?,
        source: AutomatonVertexView,
        target: AutomatonVertexView,
        labelPosition: TransitionLabelPosition = TransitionLabelPosition.COUNTER_CLOCKWISE
    ): this(edge, source.shapeType, target.shapeType, source.positionProperty, target.positionProperty, labelPosition=labelPosition)

    val transitionViews: ObservableList<TransitionView> = observableListOf()
    val oppositeEdgeProperty = objectProperty<AutomatonEdgeView?>(null)
    var oppositeEdge by oppositeEdgeProperty
    private val edgeRenderer =
        SplineAwareEdgeRenderer(
            if (sourceCenter === targetCenter) LoopEdgeRenderer(sourceShapeType, sourceCenter)
            else NonLoopEdgeRenderer(
                sourceShapeType, targetShapeType,
                sourceCenter, targetCenter,
                oppositeEdgeProperty.isNotNull,
                labelPosition
            ),
            edge
        )

    val transitionsGroup = group()

    init {
        add(edgeRenderer.edgeNode)
        edge?.transitions?.forEach { addTransition(it) }
        edge?.transitions?.addListener(SetChangeListener {
            if (it.wasAdded()) addTransition(it.elementAdded)
            if (it.wasRemoved()) removeTransition(it.elementRemoved)
        })
    }

    fun addTransition(transition: Transition) = TransitionView(transition, transitionViews.size).apply {
        rotateProperty().bind(transition.positionProperty.doubleBinding(edgeRenderer.textAngleInDegreesProperty) { if (transition.position == null) edgeRenderer.textAngleInDegreesProperty.doubleValue() else 0.0 })
        xProperty
            .bind(
                transition.positionProperty.doubleBinding(
                    edgeRenderer.midPointXProperty,
                    edgeRenderer.normXProperty,
                    indexProperty
                ) {
                    it?.x ?: (edgeRenderer.midPointX + 60 * (index + 0.5) * edgeRenderer.normX)
                })
        yProperty
            .bind(
                transition.positionProperty.doubleBinding(
                    edgeRenderer.midPointYProperty,
                    edgeRenderer.normYProperty,
                    indexProperty
                ) {
                    it?.y ?: (edgeRenderer.midPointY + 60 * (index + 0.5) * edgeRenderer.normY)
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
