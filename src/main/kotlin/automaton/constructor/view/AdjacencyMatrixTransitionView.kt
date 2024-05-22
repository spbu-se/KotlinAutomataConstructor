package automaton.constructor.view

import automaton.constructor.model.element.Transition
import tornadofx.ChangeListener
import tornadofx.label

class AdjacencyMatrixTransitionView(transition: Transition): TableTransitionView(transition) {
    init {
        label {
            textProperty().bind(transition.propertiesTextBinding)
            textLength = text.length
            textProperty().addListener(ChangeListener { _, oldValue, newValue ->
                textLength = textLength - oldValue.length + newValue.length
            })
        }
    }
}