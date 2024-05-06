package automaton.constructor.view

import automaton.constructor.controller.*
import automaton.constructor.model.action.ActionFailedException
import automaton.constructor.model.action.perform
import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.automaton.PushdownAutomaton
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
import javafx.scene.image.Image
import tornadofx.*

class MainWindow(
    openedAutomaton: Automaton = getAllAutomatonFactories().first().createAutomaton()
) : Fragment() {
    val fileController = FileController(openedAutomaton, this)
    private val helpController = HelpController()
    private val settingsController by inject<SettingsController>()
    private val layoutController = LayoutController(this)
    private val selectController = SelectController()
    private val centralViewBinding = fileController.openedAutomatonProperty.nonNullObjectBinding {
        CentralView(it, this, fileController, layoutController, windowOpener = { automatonToOpen ->
            // make sure it's a completely fresh Automaton instance independent of this window
            val newWindow = MainWindow(automatonToOpen.getData().createAutomaton())
            newWindow.layoutController.policy = layoutController.policy
            newWindow.show()
            newWindow.currentWindow
        })
    }
    private val centralView: CentralView by centralViewBinding
    private val selectedAutomatonProperty = centralViewBinding.select { it.selectedAutomatonProperty }.also {
        layoutController.selectedAutomatonProperty.bind(it)
        selectController.selectedAutomatonProperty.bind(it)
    }
    private val selectedAutomaton by selectedAutomatonProperty
    private val selectedAutomatonView get() = centralView.selectedAutomatonView
    private val undoRedoControllerProperty: Property<UndoRedoController> = centralViewBinding.select {
        it.selectedUndoRedoControllerBinding
    }
    private val undoRedoController: UndoRedoController by undoRedoControllerProperty
    private val testsControllerBinding = fileController.openedAutomatonProperty.nonNullObjectBinding {
        TestsController(it)
    }
    private val testsController: TestsController by testsControllerBinding
    private val algorithmsControllerBinding = fileController.openedAutomatonProperty.nonNullObjectBinding {
        AlgorithmsController(it, fileController, layoutController)
    }
    private val algorithmsController by algorithmsControllerBinding

    override val root = borderpane {
        top = menubar {
            menu(I18N.messages.getString("MainView.File")) {
                shortcutItem(I18N.messages.getString("MainView.File.New"), "Shortcut+N") {
                    fileController.onNew()
                }
                shortcutItem(I18N.messages.getString("MainView.File.Open"), "Shortcut+O") {
                    fileController.onOpen()
                }
                item(I18N.messages.getString("MainView.Examples")).action {
                    find<ExamplesView>(mapOf(ExamplesView::fileController to fileController)).openModal()
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
                                    error(
                                        e.message,
                                        title = I18N.messages.getString("Dialog.error"),
                                        owner = currentWindow
                                    )
                                }
                            }
                        }
                    }
                }
                fillItems()
                selectedAutomatonProperty.onChange { fillItems() }
            }
            menu(I18N.messages.getString("MainView.Select")) {
                selectController.selectors.forEach { (name, selectingFun) ->
                    item(name).action {
                        val selected = selectingFun()
                        if (selected.isEmpty()) {
                            warning(
                                I18N.messages.getString("MainView.Select.NoStatesSelected"),
                                owner = currentWindow,
                                title = I18N.messages.getString("Dialog.warning")
                            )
                        } else
                            selectedAutomatonView.automatonGraphView.selectVertices(selected)
                    }
                }
            }
            menu(I18N.messages.getString("MainView.Tests")) {
                item(I18N.messages.getString("MainView.Tests.Create")).action {
                    testsController.createTests()
                }
            }
            menu("Algorithms") {
                menu("Pushdown automaton") {
                    item("Convert into context-free grammar").action {
                        algorithmsController.convertToCFG()
                    }
                    item("Hellings algorithm").action {
                        algorithmsController.executeHellingsAlgo()
                    }
                }
            }
            menu(I18N.messages.getString("MainView.Settings")) {
                menu(I18N.messages.getString("MainView.Language")) {
                    settingsController.availableLocales.forEach { locale ->
                        item(locale.getDisplayName(locale).capitalize(locale)).action {
                            settingsController.setLocale(locale)
                            information(
                                I18N.messages.getString("LocaleController.RestartAppToApplyLanguageChange"),
                                title = I18N.messages.getString("Dialog.information"),
                                owner = currentWindow
                            )
                        }
                    }
                }
                item(I18N.messages.getString("MainView.EnableAllHints")).action {
                    settingsController.enableAllHints()
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
        icons.add(Image(this::class.java.classLoader.getResourceAsStream("icon.png")))
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
