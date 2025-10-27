package automaton.constructor.view.elements.transition

import automaton.constructor.model.element.Transition
import tornadofx.label

class AdjacencyMatrixTransitionView(transition: Transition) : TableTransitionView(transition) {
    init {
        label {
            textProperty().bind(transition.propertiesTextBinding)
            textFillProperty().bind(colorProperty)
        }
    }
}
