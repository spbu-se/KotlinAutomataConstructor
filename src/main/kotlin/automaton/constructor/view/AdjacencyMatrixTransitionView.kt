package automaton.constructor.view

import automaton.constructor.model.element.Transition
import javafx.scene.paint.Color
import tornadofx.label

class AdjacencyMatrixTransitionView(transition: Transition): TableTransitionView(transition) {
    init {
        label {
            textProperty().bind(transition.propertiesTextBinding)
            textFill = Color.BLACK
        }
    }
}