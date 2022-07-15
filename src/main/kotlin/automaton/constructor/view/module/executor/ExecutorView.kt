package automaton.constructor.view.module.executor

import automaton.constructor.controller.module.executor.ExecutorController
import automaton.constructor.model.module.executor.ExecutionState
import automaton.constructor.model.module.executor.Executor
import automaton.constructor.model.module.executor.STEPPING_STRATEGIES
import automaton.constructor.utils.SettingGroupEditor
import automaton.constructor.utils.I18N.labels
import automaton.constructor.view.memory.inputDataView
import javafx.collections.SetChangeListener
import javafx.scene.control.ScrollPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import tornadofx.*

class ExecutorView(val executor: Executor, val view: View) : HBox() {
    val controller = ExecutorController(executor, view)

    init {
        vbox {
            minWidth = USE_PREF_SIZE
            button {
                maxWidth = Double.MAX_VALUE
                maxHeight = Double.MAX_VALUE
                hgrow = Priority.ALWAYS
                vgrow = Priority.ALWAYS
                textProperty().bind(executor.startedBinding.stringBinding { if (it!!)
                    labels.getString("ExecutorView.Stop")
                else labels.getString("ExecutorView.Run") })
                action {
                    controller.toggleRun()
                }
            }
            STEPPING_STRATEGIES.forEach { steppingStrategy ->
                button(steppingStrategy.name) {
                    maxWidth = Double.MAX_VALUE
                    maxHeight = Double.MAX_VALUE
                    hgrow = Priority.ALWAYS
                    vgrow = Priority.ALWAYS
                    action {
                        controller.step(steppingStrategy)
                    }
                }
            }
        }
        scrollpane {
            maxWidth = Double.MAX_VALUE
            hgrow = Priority.SOMETIMES
            hbarPolicy = ScrollPane.ScrollBarPolicy.ALWAYS
            content = group {
                add(inputDataView(executor.automaton).apply {
                    hiddenWhen(executor.startedBinding)
                    managedWhen(visibleProperty())
                })
                hbox {
                    visibleWhen(executor.startedBinding)
                    managedWhen(visibleProperty())
                    val exePathToViewMap = mutableMapOf<ExecutionState, SettingGroupEditor>()
                    fun registerExePath(exePath: ExecutionState) {
                        val memoryView = executionLeafView(exePath)
                        exePathToViewMap[exePath] = memoryView
                        add(memoryView)
                        memoryView.minHeightProperty().bind(this@hbox.heightProperty())
                    }
                    executor.leafExecutionStates.forEach { registerExePath(it) }
                    executor.leafExecutionStates.addListener(SetChangeListener {
                        if (it.wasAdded()) registerExePath(it.elementAdded)
                        if (it.wasRemoved()) children.remove(exePathToViewMap.remove(it.elementRemoved)!!)
                    })
                }
            }
        }
    }
}
