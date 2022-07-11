package automaton.constructor.view

import automaton.constructor.controller.OpenedAutomatonController
import automaton.constructor.controller.UndoRedoController
import automaton.constructor.utils.nonNullObjectBinding
import javafx.scene.control.Menu
import javafx.scene.control.MenuItem
import javafx.scene.control.TextInputControl
import tornadofx.*

class MainView : View() {
    private val openedAutomatonController = OpenedAutomatonController(this)
    private val automatonViewBinding = openedAutomatonController.openedAutomatonProperty.nonNullObjectBinding {
        AutomatonView(it, this)
    }
    private val automatonView: AutomatonView by automatonViewBinding
    private val undoRedoControllerBinding = automatonViewBinding.nonNullObjectBinding { UndoRedoController(it) }
    private val undoRedoController: UndoRedoController by undoRedoControllerBinding

    override val root = borderpane {
        top = menubar {
            menu("File") {
                shortcutItem("New", "Shortcut+N") { openedAutomatonController.onNew() }
                shortcutItem("Open...", "Shortcut+O") { openedAutomatonController.onOpen() }
                shortcutItem("Save", "Shortcut+S") { openedAutomatonController.onSave() }
                shortcutItem("Save As...", "Shortcut+Shift+S") { openedAutomatonController.onSaveAs() }
            }
            menu("Edit") {
                shortcutItem("Undo", "Shortcut+Z") {
                    val focusOwner = scene.focusOwner
                    when {
                        focusOwner is TextInputControl && focusOwner.isUndoable -> focusOwner.undo()
                        else -> undoRedoController.onUndo()
                    }
                }.apply {
                    enableWhen(automatonView.automaton.undoRedoManager.isUndoableProperty)
                }
                shortcutItem("Redo", "Shortcut+Shift+Z") {
                    val focusOwner = scene.focusOwner
                    when {
                        focusOwner is TextInputControl && focusOwner.isRedoable -> focusOwner.redo()
                        else -> undoRedoController.onRedo()
                    }
                }.apply {
                    enableWhen(automatonView.automaton.undoRedoManager.isRedoableProperty)
                }
            }
            menu("Help") {
                shortcutItem("User Documentation", "Shortcut+D") { openedAutomatonController.onUserDocumentation() }
                shortcutItem("README", "Shortcut+R") { openedAutomatonController.onREADME() }
            }
        }
        centerProperty().bind(automatonViewBinding)
    }

    private fun Menu.shortcutItem(name: String, combo: String, action: () -> Unit): MenuItem {
        this@MainView.shortcut(combo, action)
        return item(name, combo) { action(action) }
    }

    init {
        titleProperty.bind(openedAutomatonController.openedAutomatonTitleBinding)
    }
}
