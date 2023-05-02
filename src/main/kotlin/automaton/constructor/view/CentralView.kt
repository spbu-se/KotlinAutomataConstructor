package automaton.constructor.view

import automaton.constructor.controller.FileController
import automaton.constructor.controller.LayoutController
import automaton.constructor.controller.UndoRedoController
import automaton.constructor.controller.module.executor.ExecutorController
import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.element.BuildingBlock
import automaton.constructor.utils.nonNullObjectBinding
import automaton.constructor.view.module.executor.ExecutorView
import automaton.constructor.view.module.executor.tree.ExecutionTreeView
import javafx.scene.control.SplitPane
import javafx.scene.control.TabPane
import javafx.stage.Window
import tornadofx.*
import java.util.*

/**
 * Ancestor for all UI elements other than menu bar at the top
 */
class CentralView(
    val automaton: Automaton,
    override val uiComponent: UIComponent,
    override val fileController: FileController,
    override val layoutController: LayoutController,
    private val windowOpener: (Automaton) -> Window?
) : SplitPane(),
    AutomatonViewContext {
    val mainTabView = AutomatonTabView(automaton, this)
    val automatonToTabViewMap: MutableMap<Automaton, AutomatonTabView> =
        WeakHashMap<Automaton, AutomatonTabView>().apply { put(automaton, mainTabView) }
    private lateinit var tabPane: TabPane
    val selectedAutomatonProperty = automaton.toProperty()
    var selectedAutomaton: Automaton by selectedAutomatonProperty
    val selectedAutomatonTabViewBinding = selectedAutomatonProperty.nonNullObjectBinding { getAutomatonTabView(it) }
    val selectedAutomatonTabView: AutomatonTabView by selectedAutomatonTabViewBinding
    val selectedAutomatonView get() = selectedAutomatonTabView.automatonView
    val selectedUndoRedoControllerBinding =
        selectedAutomatonTabViewBinding.nonNullObjectBinding { it.undoRedoController }
    val selectedUndoRedoController: UndoRedoController by selectedUndoRedoControllerBinding
    val executorController = ExecutorController(automaton, uiComponent).also {
        it.selectedAutomatonProperty.bind(selectedAutomatonProperty)
    }

    init {
        pane {
            tabPane = tabpane {
                tabDragPolicy = TabPane.TabDragPolicy.REORDER
                tabClosingPolicy = TabPane.TabClosingPolicy.ALL_TABS
                tab(tag = automaton) {
                    content = mainTabView
                    isClosable = false
                    textProperty().bind(automaton.nameProperty)
                }
                selectionModel.selectedItemProperty().onChange { selectedAutomatonTabView.ensureAutomatonViewIsShown() }
            }
            selectedAutomatonProperty.bind(
                tabPane.selectionModel.selectedItemProperty().nonNullObjectBinding { it.tag as Automaton }
            )
            val executorView = ExecutorView(executorController)
            add(executorView)
            executorView.prefWidthProperty().bind(this@pane.widthProperty())
            executorView.layoutYProperty().bind(this@pane.heightProperty() - executorView.heightProperty())
            tabPane.prefWidthProperty().bind(this@pane.widthProperty())
            tabPane.prefHeightProperty().bind(this@pane.heightProperty() - executorView.heightProperty())
        }
        executorController.debuggingExecutorProperty.onChange { executor ->
            if (items.size > 1) items.removeLast()
            if (executor != null) {
                items.add(ExecutionTreeView(executor))
                setDividerPosition(0, 0.7)
            }
        }
    }

    override fun openInNewWindow(automaton: Automaton) = windowOpener(automaton)

    private fun getAutomatonTabView(automaton: Automaton) = automatonToTabViewMap.getOrPut(automaton) {
        AutomatonTabView(automaton, this)
    }

    override fun getAutomatonView(automaton: Automaton): AutomatonView = getAutomatonTabView(automaton).automatonView

    override fun onBuildingBlockDoubleClicked(buildingBlock: BuildingBlock) {
        val tab = tabPane.tabs.firstOrNull { it.tag == buildingBlock.subAutomaton } ?: run {
            tabPane.tab(tag = buildingBlock.subAutomaton) {
                textProperty().bind(buildingBlock.subAutomaton.nameProperty)
                content = getAutomatonTabView(buildingBlock.subAutomaton)
            }
        }
        tabPane.selectionModel.select(tab)
    }
}
