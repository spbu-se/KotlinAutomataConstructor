package automaton.constructor.view.elements.transition

import automaton.constructor.model.element.Transition
import automaton.constructor.model.element.RecursiveAutomatonBox
import tornadofx.label

class AdjacencyMatrixTransitionView(transition: Transition): TableTransitionView(transition) {
    init {
        val hideLabel = transition.source is RecursiveAutomatonBox || transition.target is RecursiveAutomatonBox
        label {
            if (!hideLabel) textProperty().bind(transition.propertiesTextBinding) else text = ""
            textFillProperty().bind(colorProperty)
        }
    }
}
