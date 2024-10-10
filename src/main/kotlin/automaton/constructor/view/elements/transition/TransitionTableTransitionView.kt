package automaton.constructor.view.elements.transition

import automaton.constructor.model.element.Transition
import javafx.scene.paint.Color
import tornadofx.*

class TransitionTableTransitionView(transition: Transition): TableTransitionView(transition) {
    init {
        hbox {
            label {
                textProperty().bind(transition.filtersTextBinding)
                textFill = Color.BLACK
            }
            if (transition.sideEffectsText.isNotEmpty()) {
                label("→") {
                    textFill = Color.BLACK
                }
                label {
                    textProperty().bind(transition.sideEffectsTextBinding)
                    textFill = Color.BLACK
                }
            }
        }
    }
}
