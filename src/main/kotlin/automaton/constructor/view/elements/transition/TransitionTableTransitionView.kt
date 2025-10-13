package automaton.constructor.view.elements.transition

import automaton.constructor.model.element.Transition
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
