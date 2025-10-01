package automaton.constructor.view.elements.transition

import automaton.constructor.model.element.Transition
import automaton.constructor.model.element.RecursiveAutomatonBox
import automaton.constructor.utils.nonNullObjectBinding
import automaton.constructor.utils.translateToCenter
import javafx.beans.binding.Binding
import javafx.beans.property.DoubleProperty
import javafx.scene.paint.Color
import javafx.scene.text.Font
import tornadofx.getValue
import tornadofx.setValue
import tornadofx.text
import tornadofx.toProperty

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
        val hideLabel = transition.source is RecursiveAutomatonBox || transition.target is RecursiveAutomatonBox
        val text = text {
            fillProperty().bind(colorProperty)
            font = Font.font(48.0)
            if (hideLabel) this.text = "" else textProperty().bind(transition.propertiesTextBinding)
            translateToCenter()
        }
        xProperty = text.xProperty()
        yProperty = text.yProperty()
    }
}
