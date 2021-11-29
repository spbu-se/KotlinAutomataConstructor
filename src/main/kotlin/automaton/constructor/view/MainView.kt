package automaton.constructor.view

import automaton.constructor.model.Automaton
import automaton.constructor.model.memory.tape.InputTapeDescriptor
import tornadofx.*

class MainView : View() {
    private val automatonViewProperty = AutomatonView(Automaton(listOf(InputTapeDescriptor())), this).toProperty()
    private val automatonView: AutomatonView by automatonViewProperty

    override val root = borderpane {
        centerProperty().bind(automatonViewProperty)
    }
}
