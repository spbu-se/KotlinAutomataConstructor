package automaton.constructor.view

import automaton.constructor.controller.AutomatonDocumentationController
import automaton.constructor.controller.FileController
import automaton.constructor.controller.UndoRedoController
import automaton.constructor.model.action.ActionFailedException
import automaton.constructor.model.action.perform
import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.factory.getAllAutomatonFactories
import automaton.constructor.utils.I18N
import automaton.constructor.utils.nonNullObjectBinding
import javafx.beans.property.Property
import javafx.scene.control.Menu
import javafx.scene.control.MenuItem
import tornadofx.*

class MainWindow(openedAutomaton: Automaton = getAllAutomatonFactories().first().createAutomaton()) : Fragment() {
    private val fileController = FileController(openedAutomaton, this)
    private val automatonDocumentationController = AutomatonDocumentationController()
    private val centralViewBinding = fileController.openedAutomatonProperty.nonNullObjectBinding {
        CentralView(it, fileController)
    }
    private val centralView: CentralView by centralViewBinding
    private val undoRedoControllerProperty: Property<UndoRedoController> = centralViewBinding.select {
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
                    enableWhen(undoRedoControllerProperty.select { it.isUndoableBinding })
                }
                item(I18N.messages.getString("MainView.Edit.Redo"), UndoRedoController.REDO_COMBO) {
                    action { undoRedoController.onRedo() }
                }.apply {
                    enableWhen(undoRedoControllerProperty.select { it.isRedoableBinding })
                }
            }
            menu(I18N.messages.getString("MainView.Transform")) {
                fun fillItems() {
                    items.clear()
                    fileController.openedAutomaton.transformationActions.forEach {
                        item(it.displayName) {
                            action {
                                try {
                                    it.perform()
                                } catch (e: ActionFailedException) {
                                    error(e.message)
                                }
                            }
                        }
                    }
                }
                fillItems()
                centralViewBinding.select { it.selectedAutomatonProperty }.onChange { fillItems() }
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
        centerProperty().bind(centralViewBinding)
    }

    private fun Menu.shortcutItem(name: String, combo: String, action: () -> Unit): MenuItem {
        this@MainWindow.shortcut(combo, action)
        return item(name, combo) { action(action) }
    }

    init {
        titleProperty.bind(fileController.openedAutomatonTitleBinding)
    }

    fun show() = openWindow(
        owner = null,
        escapeClosesWindow = false,
    )?.apply {
        width = 1000.0
        height = 600.0
        centerOnScreen()
        runLater {
            setOnCloseRequest {
                if (!fileController.suggestSavingChanges()) it.consume()
            }
            isMaximized = true
        }
    }
}
