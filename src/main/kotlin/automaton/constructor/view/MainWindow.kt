package automaton.constructor.view

import automaton.constructor.controller.*
import automaton.constructor.model.action.ActionFailedException
import automaton.constructor.model.action.perform
import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.data.createAutomaton
import automaton.constructor.model.data.getData
import automaton.constructor.model.factory.getAllAutomatonFactories
import automaton.constructor.model.module.layout.dynamic.DynamicLayoutPolicy
import automaton.constructor.model.module.layout.static.STATIC_LAYOUTS
import automaton.constructor.utils.I18N
import automaton.constructor.utils.capitalize
import automaton.constructor.utils.nonNullObjectBinding
import javafx.beans.property.Property
import javafx.scene.control.Menu
import javafx.scene.control.MenuItem
import javafx.scene.control.ToggleGroup
import tornadofx.*

class MainWindow(
    openedAutomaton: Automaton = getAllAutomatonFactories().first().createAutomaton()
) : Fragment() {
    val fileController = FileController(openedAutomaton, this)
    private val helpController = HelpController()
    private val localeController = find<LocaleController>()
    private val layoutController = LayoutController(this)
    private val centralViewBinding = fileController.openedAutomatonProperty.nonNullObjectBinding {
        CentralView(it, fileController, layoutController, windowOpener = { automatonToOpen ->
            // make sure it's a completely fresh Automaton instance independent of this window
            MainWindow(automatonToOpen.getData().createAutomaton()).also { newWindow ->
                newWindow.show()
                newWindow.layoutController.policy = layoutController.policy
            }
        })
    }
    private val centralView: CentralView by centralViewBinding
    private val selectedAutomatonProperty = centralViewBinding.select { it.selectedAutomatonProperty }.also {
        layoutController.selectedAutomatonProperty.bind(it)
    }
    private val selectedAutomaton by selectedAutomatonProperty
    private val selectedAutomatonView get() = centralView.selectedAutomatonView
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
            menu(I18N.messages.getString("MainView.Layout")) {
                STATIC_LAYOUTS.forEach { layout ->
                    item(layout.name).action {
                        layoutController.layout(
                            selectedAutomaton,
                            selectedAutomatonView.automatonGraphView.transitionLayoutBounds(),
                            layout
                        )
                    }
                }
                separator()
                val policyToggleGroup = ToggleGroup()
                DynamicLayoutPolicy.values().forEach { policy ->
                    radiomenuitem(policy.displayName, policyToggleGroup) {
                        isSelected = policy == layoutController.policy
                        action {
                            if (isSelected) layoutController.policy = policy
                        }
                        layoutController.policyProperty.onChange {
                            if (it == policy)
                                isSelected = true
                        }
                    }
                }
                separator()
                item(I18N.messages.getString("MainView.Layout.UndoDynamic")).action {
                    layoutController.undoDynamicLayout()
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
            menu(I18N.messages.getString("MainView.Language")) {
                localeController.availableLocales.forEach { locale ->
                    item(locale.getDisplayName(locale).capitalize(locale)).action {
                        localeController.setLocale(locale)
                    }
                }
            }
            menu(I18N.messages.getString("MainView.Help")) {
                shortcutItem(I18N.messages.getString("MainView.Help.UserDocumentation"), "F1") {
                    helpController.onUserDocumentation()
                }
                item(I18N.messages.getString("MainView.Help.README")) {
                    action {
                        helpController.onREADME()
                    }
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
                else layoutController.stopDynamicLayout()
            }
            isMaximized = true
        }
    }.also { layoutController.startDynamicLayout() }
}
