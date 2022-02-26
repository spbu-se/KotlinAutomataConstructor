package automaton.constructor.view

import automaton.constructor.controller.OpenedAutomatonController
import automaton.constructor.utils.nonNullObjectBinding
import javafx.scene.control.Menu
import javafx.scene.input.KeyCombination
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
                shortcutItem("New", KeyCombination.valueOf("Shortcut+N")) { openedAutomatonController.onNew() }
                shortcutItem("Open...", KeyCombination.valueOf("Shortcut+O")) { openedAutomatonController.onOpen() }
                shortcutItem("Save", KeyCombination.valueOf("Shortcut+S")) { openedAutomatonController.onSave() }
                shortcutItem(
                    "Save As...",
                    KeyCombination.valueOf("Shortcut+Shift+S")
                ) { openedAutomatonController.onSaveAs() }
            }
        }
        centerProperty().bind(automatonViewBinding)
    }

    private fun Menu.shortcutItem(name: String, combo: KeyCombination, action: () -> Unit) {
        item(name, combo).action(action)
        this@MainView.shortcut(combo, action)
    }

    init {
        titleProperty.bind(openedAutomatonController.openedAutomatonTitleBinding)
    }
}
