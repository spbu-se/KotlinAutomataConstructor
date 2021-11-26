package automaton.constructor.view

import automaton.constructor.model.Automaton
import automaton.constructor.model.State
import automaton.constructor.model.memory.tape.InputTape
import javafx.geometry.Point2D
import tornadofx.*

class MainView : View() {
    private val automatonViewProperty = AutomatonView(Automaton(listOf(InputTape())), this).toProperty()
    private val automatonView: AutomatonView by automatonViewProperty

    override val root = borderpane {
        centerProperty().bind(automatonViewProperty)
    }
}
