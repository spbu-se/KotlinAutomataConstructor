package automaton.constructor.view

import automaton.constructor.controller.AutomatonDocumentationController
import automaton.constructor.controller.OpenedAutomatonController
import automaton.constructor.controller.UndoRedoController
import automaton.constructor.utils.nonNullObjectBinding
import automaton.constructor.utils.I18N.labels
import javafx.scene.control.Menu
import javafx.scene.control.MenuItem
import javafx.scene.control.TextInputControl
import tornadofx.*

class MainView : View() {
    private val openedAutomatonController = OpenedAutomatonController(this)
    private val automatonDocumentationController = AutomatonDocumentationController()
    private val automatonViewBinding = openedAutomatonController.openedAutomatonProperty.nonNullObjectBinding {
        AutomatonView(it, this)
    }
    private val automatonView: AutomatonView by automatonViewBinding
    private val undoRedoControllerBinding = automatonViewBinding.nonNullObjectBinding { UndoRedoController(it) }
    private val undoRedoController: UndoRedoController by undoRedoControllerBinding

    override val root = borderpane {
        top = menubar {
            menu(labels.getString("MainView.File")) {
                shortcutItem(labels.getString("MainView.File.New"), "Shortcut+N") {
                    openedAutomatonController.onNew()
                }
                shortcutItem(labels.getString("MainView.File.Open"), "Shortcut+O") {
                    openedAutomatonController.onOpen()
                }
                shortcutItem(labels.getString("MainView.File.Save"), "Shortcut+S") {
                    openedAutomatonController.onSave()
                }
                shortcutItem(labels.getString("MainView.File.SaveAs"), "Shortcut+Shift+S") {
                    openedAutomatonController.onSaveAs()
                }
            }
            menu(labels.getString("MainView.Edit")) {
                shortcutItem(labels.getString("MainView.Edit.Undo"), "Shortcut+Z") {
                    val focusOwner = scene.focusOwner
                    when {
                        focusOwner is TextInputControl && focusOwner.isUndoable -> focusOwner.undo()
                        else -> undoRedoController.onUndo()
                    }
                }.apply {
                    enableWhen(automatonView.automaton.undoRedoManager.isUndoableProperty)
                }
                shortcutItem(labels.getString("MainView.Edit.Redo"), "Shortcut+Shift+Z") {
                    val focusOwner = scene.focusOwner
                    when {
                        focusOwner is TextInputControl && focusOwner.isRedoable -> focusOwner.redo()
                        else -> undoRedoController.onRedo()
                    }
                }.apply {
                    enableWhen(automatonView.automaton.undoRedoManager.isRedoableProperty)
                }
            }
            menu(labels.getString("MainView.Help")) {
                shortcutItem(labels.getString("MainView.Help.UserDocumentation"), "Shortcut+D") {
                    automatonDocumentationController.onUserDocumentation()
                }
                shortcutItem(labels.getString("MainView.Help.README"), "Shortcut+R") {
                    automatonDocumentationController.onREADME()
                }
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
