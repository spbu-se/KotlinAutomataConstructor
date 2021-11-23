package automaton.constructor.view

import automaton.constructor.model.State
import automaton.constructor.utils.*
import javafx.beans.binding.Binding
import javafx.scene.Group
import javafx.scene.control.CheckBox
import javafx.scene.control.TextField
import javafx.scene.paint.Color
import javafx.scene.text.Font
import tornadofx.*

class StateView(val state: State) : Group(), SettingsHolder {
    val selectedProperty = false.toProperty()
    var selected by selectedProperty

    val colorProperty: Binding<Color> = nonNullObjectBinding(selectedProperty, state.isCurrentProperty) {
        when {
            selected && state.isCurrent -> Color.PURPLE
            !selected && state.isCurrent -> Color.SADDLEBROWN
            selected && !state.isCurrent -> Color.AQUA
            else -> Color.YELLOW
        }
    }

    val initialStateMarker = polygon(0.0, 0.0, 0.0, 0.0, 0.0, 0.0) {
        visibleWhen(state.isInitialProperty)
        fun updatePosition() {
            points[0] = state.position.x - STATE_RADIUS
            points[1] = state.position.y
            points[2] = state.position.x - 1.5 * STATE_RADIUS
            points[3] = state.position.y - STATE_RADIUS / 2.0
            points[4] = state.position.x - 1.5 * STATE_RADIUS
            points[5] = state.position.y + STATE_RADIUS / 2.0
        }
        updatePosition()
        state.positionProperty.onChange { updatePosition() }
        fill = Color.LIGHTGRAY
        stroke = Color.BLACK
    }

    val circle = circle {
        centerXProperty().bind(state.positionProperty.x)
        centerYProperty().bind(state.positionProperty.y)
        fillProperty().bind(colorProperty)
        stroke = Color.BLACK
        radius = STATE_RADIUS
    }

    val finalOutline = circle {
        visibleWhen(state.isFinalProperty)
        centerXProperty().bind(state.positionProperty.x)
        centerYProperty().bind(state.positionProperty.y)
        fillProperty().bind(colorProperty)
        stroke = Color.BLACK
        radius = STATE_RADIUS - STATE_RADIUS / 5
    }

    val text = text {
        font = Font.font(48.0) // TODO maybe dynamically adjust font size so text fits inside a circle
        textProperty().bind(state.nameProperty)
        translateToCenter()
        xProperty().bind(state.positionProperty.x)
        yProperty().bind(state.positionProperty.y)
    }

    override fun getSettings() = listOf(
        SettingGroup(
            "State".toProperty(), listOf(
                Setting("Name", TextField().apply { textProperty().bindBidirectional(state.nameProperty) }),
                Setting("Initial", CheckBox().apply { selectedProperty().bindBidirectional(state.isInitialProperty) }),
                Setting("Final", CheckBox().apply { selectedProperty().bindBidirectional(state.isFinalProperty) }),
            )
        )
    )
}
