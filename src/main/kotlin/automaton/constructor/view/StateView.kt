package automaton.constructor.view

import automaton.constructor.model.State
import automaton.constructor.utils.*
import javafx.beans.property.Property
import javafx.geometry.Point2D
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.control.CheckBox
import javafx.scene.control.TextField
import javafx.scene.paint.Color
import javafx.scene.text.Font
import tornadofx.*

class StateView(val state: State) : AutomatonElementView(state) {

    companion object {
        const val RADIUS = 50.0
        val DEFAULT_COLOR: Color get() = Color.YELLOW
    }

    val positionProperty: Property<Point2D> = state.position.toProperty().apply { bind(state.positionProperty) }
    val colorProperty: Property<Color> = DEFAULT_COLOR.toProperty().apply {
        bind(selectedProperty.nonNullObjectBinding(state.isCurrentBinding) {
            when {
                selected && state.isCurrent -> Color.PURPLE
                !selected && state.isCurrent -> Color.SADDLEBROWN
                selected && !state.isCurrent -> Color.AQUA
                else -> DEFAULT_COLOR
            }
        })
    }
    val initMarker: Node
    val group = Group().apply {
        initMarker = polygon(0.0, 0.0, 0.0, 0.0, 0.0, 0.0) {
            visibleWhen(state.isInitialProperty)
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

        circle {
            centerXProperty().bind(positionProperty.x)
            centerYProperty().bind(positionProperty.y)
            fillProperty().bind(colorProperty)
            stroke = Color.BLACK
            radius = RADIUS
        }

        circle {
            visibleWhen(state.isFinalProperty)
            centerXProperty().bind(positionProperty.x)
            centerYProperty().bind(positionProperty.y)
            fillProperty().bind(colorProperty)
            stroke = Color.BLACK
            radius = RADIUS - RADIUS / 5
        }

        text {
            font = Font.font(48.0) // TODO maybe dynamically adjust font size so text fits inside a circle
            textProperty().bind(stringBinding(state.nameProperty, settingsTextBinding) {
                listOf(state.nameProperty.value, settingsTextBinding.value).filter { it.isNotEmpty() }
                    .joinToString(separator = "/")
            })
            translateToCenter()
            xProperty().bind(positionProperty.x)
            yProperty().bind(positionProperty.y)
        }
    }

    override fun getSettings() = listOf(
        SettingGroup(
            "State".toProperty(), listOf(
                Setting("Name", TextField().apply { textProperty().bindBidirectional(state.nameProperty) }),
                Setting("Initial", CheckBox().apply { selectedProperty().bindBidirectional(state.isInitialProperty) }),
                Setting("Final", CheckBox().apply { selectedProperty().bindBidirectional(state.isFinalProperty) }),
            )
        )
    ) + super.getSettings()
}
