package automaton.constructor.view

import automaton.constructor.model.element.Transition
import automaton.constructor.utils.*
import javafx.beans.binding.Binding
import javafx.beans.property.DoubleProperty
import javafx.scene.paint.Color
import javafx.scene.text.Font
import tornadofx.*

class TransitionView(
    transition: Transition,
    index: Int
) : BasicTransitionView(transition) {
    val indexProperty = index.toProperty()
    var index by indexProperty
    val xProperty: DoubleProperty
    val yProperty: DoubleProperty

    val colorProperty: Binding<Color> = selectedProperty.nonNullObjectBinding {
        if (selected) Color.BLUE else Color.BLACK
    }

    init {
        val text = text {
            fillProperty().bind(colorProperty)
            font = Font.font(48.0)
            textProperty().bind(transition.propertiesTextBinding)
            translateToCenter()
        }
        xProperty = text.xProperty()
        yProperty = text.yProperty()
    }
}
