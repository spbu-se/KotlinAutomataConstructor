package automaton.constructor.view.elements.transition

import automaton.constructor.model.element.Transition
import automaton.constructor.model.element.RecursiveAutomatonBox
import tornadofx.*

class TransitionTableTransitionView(transition: Transition): TableTransitionView(transition) {
    init {
        val hideLabel = transition.source is RecursiveAutomatonBox || transition.target is RecursiveAutomatonBox
        hbox {
            if (!hideLabel) {
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
}
