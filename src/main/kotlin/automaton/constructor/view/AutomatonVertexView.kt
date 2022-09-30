package automaton.constructor.view

import automaton.constructor.model.element.AutomatonVertex
import automaton.constructor.model.element.AutomatonVertex.Companion.RADIUS
import automaton.constructor.model.element.BuildingBlock
import automaton.constructor.model.element.State
import automaton.constructor.model.module.hasProblems
import automaton.constructor.model.module.hasProblemsBinding
import automaton.constructor.utils.*
import automaton.constructor.utils.I18N.messages
import automaton.constructor.view.AutomatonVertexView.ShapeType.CIRCLE
import automaton.constructor.view.AutomatonVertexView.ShapeType.SQUARE
import javafx.beans.property.Property
import javafx.geometry.Point2D
import javafx.geometry.VPos
import javafx.scene.Node
import javafx.scene.control.CheckBox
import javafx.scene.control.TextField
import javafx.scene.paint.Color
import javafx.scene.shape.Shape
import javafx.scene.text.Font.font
import javafx.scene.text.TextAlignment
import tornadofx.*

class AutomatonVertexView(val vertex: AutomatonVertex) : AutomatonElementView(vertex) {
    val positionProperty: Property<Point2D> = vertex.position.toProperty().apply { bind(vertex.positionProperty) }
    val colorProperty: Property<Color> = DEFAULT_COLOR.toProperty().apply {
        val colorBinding = selectedProperty.nonNullObjectBinding(vertex.isCurrentBinding) {
            when {
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
            fontProperty().bind(
                vertex.nameProperty.objectBinding {
                    font(if (it!!.length < 4) 48.0 else 32.0)
                }
            )
            textProperty().bind(stringBinding(vertex.nameProperty, settingsTextBinding) {
                listOf(vertex.nameProperty.value, settingsTextBinding.value).filter { it.isNotEmpty() }
                    .joinToString(separator = "/")
            })
            translateXProperty().bind(layoutBoundsProperty().doubleBinding { -it!!.width / 2 })
            translateYProperty().bind(baselineOffsetProperty().doubleBinding { -it!!.toDouble() / 10 })
            xProperty().bind(positionProperty.x)
            yProperty().bind(positionProperty.y)
        }
    }

    override fun getSettings() = listOf(
        SettingGroup(
            messages.getString("StateView.State").toProperty(), listOf(
                Setting(messages.getString("StateView.Name"),
                    TextField().apply { textProperty().bindBidirectional(vertex.nameProperty) }),
                Setting(messages.getString("StateView.Initial"),
                    CheckBox().apply { selectedProperty().bindBidirectional(vertex.isInitialProperty) })
            ) + if (vertex.alwaysEffectivelyFinal) emptyList() else listOf(
                Setting(messages.getString("StateView.Final"),
                    CheckBox().apply { selectedProperty().bindBidirectional(vertex.isFinalProperty) })
            )
        )
    ) + super.getSettings()

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
    }
}
