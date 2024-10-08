package automaton.constructor.view

import automaton.constructor.model.element.Transition
import javafx.scene.paint.Color
import tornadofx.*

class TransitionTableTransitionView(transition: Transition): TableTransitionView(transition) {
    init {
        hbox {
            label {
                textProperty().bind(transition.filtersTextBinding)
                textFillProperty().bind(colorProperty)
            }
            if (transition.sideEffectsText.isNotEmpty()) {
                label("→") {
                    textFillProperty().bind(colorProperty)
                }
                label {
                    textProperty().bind(transition.sideEffectsTextBinding)
                    textFillProperty().bind(colorProperty)
                }
            }
        }
    }
}
