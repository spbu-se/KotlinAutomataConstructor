package automaton.constructor.view

import automaton.constructor.controller.OpenedAutomatonController
import automaton.constructor.utils.nonNullObjectBinding
import tornadofx.*

class MainView : View() {
    private val openedAutomatonController = OpenedAutomatonController(this)
    private val automatonViewBinding = openedAutomatonController.openedAutomatonProperty.nonNullObjectBinding {
        AutomatonView(it, this)
    }
    private val automatonView: AutomatonView by automatonViewBinding

    override val root = borderpane {
        top = menubar {
            menu("File") {
                item("New") {
                    action {
                        openedAutomatonController.onNewClicked()
                    }
                }
            }
        }
        centerProperty().bind(automatonViewBinding)
    }
}
