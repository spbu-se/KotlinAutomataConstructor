package automaton.constructor.view

import automaton.constructor.model.element.AutomatonVertex
import automaton.constructor.model.element.AutomatonVertex.Companion.RADIUS
import automaton.constructor.model.element.BuildingBlock
import automaton.constructor.model.element.State
import automaton.constructor.model.module.hasProblems
import automaton.constructor.model.module.hasProblemsBinding
import automaton.constructor.utils.*
import automaton.constructor.view.AutomatonVertexView.ShapeType.CIRCLE
import automaton.constructor.view.AutomatonVertexView.ShapeType.SQUARE
import javafx.beans.property.Property
import javafx.geometry.Point2D
import javafx.geometry.VPos
import javafx.scene.Node
import javafx.scene.paint.Color
import javafx.scene.shape.Shape
import javafx.scene.text.Font.font
import javafx.scene.text.TextAlignment
import tornadofx.*
import kotlin.math.abs
import kotlin.math.max

class AutomatonVertexView(vertex: AutomatonVertex) : AutomatonBasicVertexView(vertex) {
    val positionProperty: Property<Point2D> = vertex.position.toProperty().apply { bind(vertex.positionProperty) }
    val colorProperty: Property<Color> = DEFAULT_COLOR.toProperty().apply {
        val colorBinding =
            selectedProperty.nonNullObjectBinding(vertex.isCurrentBinding, vertex.isHighlightedProperty) {
                when {
                    vertex.isHighlighted -> Color.DARKORANGE
                    selected && vertex.isCurrent -> Color.PURPLE
                    !selected && vertex.isCurrent -> Color.SADDLEBROWN
                    selected && !vertex.isCurrent -> Color.AQUA
                    else -> DEFAULT_COLOR
                }
            }
        bind(when (vertex) {
            is State -> colorBinding
            is BuildingBlock -> colorBinding.nonNullObjectBinding(vertex.subAutomaton.hasProblemsBinding) {
                if (vertex.subAutomaton.hasProblems) Color.RED
                else colorBinding.value
            }
        })
    }
    val shapeType = when (vertex) {
        is State -> CIRCLE
        is BuildingBlock -> SQUARE
    }

    val initMarker: Node
    val finalMarker: Node
    val mainNode: Node

    init {
        initMarker = polygon(0.0, 0.0, 0.0, 0.0, 0.0, 0.0) {
            visibleWhen(vertex.isInitialProperty)
            fun updatePosition() {
                points[0] = positionProperty.value.x - RADIUS
                points[1] = positionProperty.value.y
                points[2] = positionProperty.value.x - 1.5 * RADIUS
                points[3] = positionProperty.value.y - RADIUS / 2.0
                points[4] = positionProperty.value.x - 1.5 * RADIUS
                points[5] = positionProperty.value.y + RADIUS / 2.0
            }
            updatePosition()
            positionProperty.onChange { updatePosition() }
            fill = Color.LIGHTGRAY
            stroke = Color.BLACK
        }

        finalMarker = placeShape(RADIUS) {
            fillProperty().bind(colorProperty)
            stroke = Color.BLACK
        }

        mainNode = placeShape(RADIUS - RADIUS / 5) {
            visibleWhen(vertex.isFinalProperty)
            fillProperty().bind(colorProperty)
            stroke = Color.BLACK
        }

        text {
            textAlignment = TextAlignment.CENTER
            textOrigin = VPos.CENTER
            wrappingWidth = RADIUS * 2
            textProperty().bind(stringBinding(vertex.nameProperty, vertex.propertiesTextBinding) {
                listOf(vertex.name, vertex.propetiesText).filter { it.isNotEmpty() }
                    .joinToString(separator = "/")
            })
            fontProperty().bind(textProperty().objectBinding {
                font(
                    when {
                        it!!.length < 4 -> 48.0
                        it.length < 6 -> 32.0
                        else -> 24.0
                    }
                )
            })
            translateXProperty().bind(layoutBoundsProperty().doubleBinding { -it!!.width / 2 })
            translateYProperty().bind(baselineOffsetProperty().doubleBinding { -it!!.toDouble() / 10 })
            xProperty().bind(positionProperty.x)
            yProperty().bind(positionProperty.y)
        }
    }

    private fun placeShape(radius: Double, op: Shape.() -> Unit) = when (shapeType) {
        CIRCLE -> circle {
            this.radius = radius
            centerXProperty().bind(positionProperty.x)
            centerYProperty().bind(positionProperty.y)
            op()
        }
        SQUARE -> rectangle {
            xProperty().bind(positionProperty.x - radius)
            yProperty().bind(positionProperty.y - radius)
            width = radius * 2
            height = radius * 2
            op()
        }
    }


    companion object {
        val DEFAULT_COLOR: Color = Color.YELLOW
    }

    enum class ShapeType {
        CIRCLE, SQUARE;

        fun project(shapeCenter: Point2D, point: Point2D): Point2D {
            val v = shapeCenter - point
            return if (v == Vector2D.ZERO) point
            else shapeCenter - RADIUS * when (this) {
                CIRCLE -> v.normalize()
                SQUARE -> v / max(abs(v.x), abs(v.y))
            }
        }
    }
}
