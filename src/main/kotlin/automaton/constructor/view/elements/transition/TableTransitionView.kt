package automaton.constructor.view.elements.transition

import automaton.constructor.model.element.Transition
import automaton.constructor.utils.*
import javafx.beans.binding.Binding
import javafx.scene.paint.Color

open class TableTransitionView(transition: Transition): BasicTransitionView(transition) {
    val colorProperty: Binding<Color> = selectedProperty.nonNullObjectBinding {
        if (selected) Color.AQUA else Color.BLACK
    }
}
