package automaton.constructor.view

import automaton.constructor.controller.AutomatonDocumentationController
import automaton.constructor.controller.FileController
import automaton.constructor.controller.UndoRedoController
import automaton.constructor.utils.I18N
import automaton.constructor.utils.nonNullObjectBinding
import javafx.beans.property.Property
import javafx.scene.control.Menu
import javafx.scene.control.MenuItem
import tornadofx.*

class MainView : View() {
    private val fileController = FileController(this)
    private val automatonDocumentationController = AutomatonDocumentationController()
    private val mainAutomatonViewBinding = fileController.openedAutomatonProperty.nonNullObjectBinding {
        MainAutomatonView(it, fileController)
    }
    private val mainAutomatonView: MainAutomatonView by mainAutomatonViewBinding
    private val undoRedoControllerProperty: Property<UndoRedoController> = mainAutomatonViewBinding.select {
        it.selectedUndoRedoControllerBinding
    }
    private val undoRedoController: UndoRedoController by undoRedoControllerProperty

    override val root = borderpane {
        top = menubar {
            menu(I18N.messages.getString("MainView.File")) {
                shortcutItem(I18N.messages.getString("MainView.File.New"), "Shortcut+N") {
                    fileController.onNew()
                }
                shortcutItem(I18N.messages.getString("MainView.File.Open"), "Shortcut+O") {
                    fileController.onOpen()
                }
                shortcutItem(I18N.messages.getString("MainView.File.Save"), "Shortcut+S") {
                    fileController.onSave()
                }
                shortcutItem(I18N.messages.getString("MainView.File.SaveAs"), "Shortcut+Shift+S") {
                    fileController.onSaveAs()
                }
            }
            menu(I18N.messages.getString("MainView.Edit")) {
                item(I18N.messages.getString("MainView.Edit.Undo"), UndoRedoController.UNDO_COMBO) {
                    action { undoRedoController.onUndo() }
                }.apply {
                    enableWhen(undoRedoControllerProperty.select { it.isUndoableProperty })
                }
                item(I18N.messages.getString("MainView.Edit.Redo"), UndoRedoController.REDO_COMBO) {
                    action { undoRedoController.onRedo() }
                }.apply {
                    enableWhen(undoRedoControllerProperty.select { it.isRedoableProperty })
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
        centerProperty().bind(mainAutomatonViewBinding)
    }

    private fun Menu.shortcutItem(name: String, combo: String, action: () -> Unit): MenuItem {
        this@MainView.shortcut(combo, action)
        return item(name, combo) { action(action) }
    }

    init {
        titleProperty.bind(fileController.openedAutomatonTitleBinding)
    }
}
