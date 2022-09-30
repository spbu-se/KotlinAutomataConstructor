package automaton.constructor.view

import automaton.constructor.controller.AutomatonDocumentationController
import automaton.constructor.controller.OpenedAutomatonController
import automaton.constructor.controller.UndoRedoController
import automaton.constructor.utils.I18N
import automaton.constructor.utils.nonNullObjectBinding
import javafx.scene.control.Menu
import javafx.scene.control.MenuItem
import tornadofx.*

class MainView : View() {
    private val openedAutomatonController = OpenedAutomatonController(this)
    private val automatonDocumentationController = AutomatonDocumentationController()
    private val automatonViewBinding = openedAutomatonController.openedAutomatonProperty.nonNullObjectBinding {
        AutomatonView(it)
    }
    private val automatonView: AutomatonView by automatonViewBinding
    private val undoRedoControllerBinding = automatonViewBinding.nonNullObjectBinding { UndoRedoController(it) }
    private val undoRedoController: UndoRedoController by undoRedoControllerBinding

    override val root = borderpane {
        top = menubar {
            menu(I18N.messages.getString("MainView.File")) {
                shortcutItem(I18N.messages.getString("MainView.File.New"), "Shortcut+N") {
                    openedAutomatonController.onNew()
                }
                shortcutItem(I18N.messages.getString("MainView.File.Open"), "Shortcut+O") {
                    openedAutomatonController.onOpen()
                }
                shortcutItem(I18N.messages.getString("MainView.File.Save"), "Shortcut+S") {
                    openedAutomatonController.onSave()
                }
                shortcutItem(I18N.messages.getString("MainView.File.SaveAs"), "Shortcut+Shift+S") {
                    openedAutomatonController.onSaveAs()
                }
            }
            menu(I18N.messages.getString("MainView.Edit")) {
                item(I18N.messages.getString("MainView.Edit.Undo"), UndoRedoController.UNDO_COMBO) {
                    action { undoRedoController.onUndo() }
                }.apply {
                    enableWhen(automatonViewBinding.select { it.automaton.undoRedoManager.isUndoableProperty })
                }
                item(I18N.messages.getString("MainView.Edit.Redo"), UndoRedoController.REDO_COMBO) {
                    action { undoRedoController.onRedo() }
                }.apply {
                    enableWhen(automatonViewBinding.select { it.automaton.undoRedoManager.isRedoableProperty })
                }
            }
            menu(I18N.messages.getString("MainView.Help")) {
                shortcutItem(I18N.messages.getString("MainView.Help.UserDocumentation"), "Shortcut+D") {
                    automatonDocumentationController.onUserDocumentation()
                }
                shortcutItem(I18N.messages.getString("MainView.Help.README"), "Shortcut+R") {
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
