package automaton.constructor.view.module.executor

import automaton.constructor.controller.module.executor.ExecutorController
import automaton.constructor.model.module.executor.ExecutionPath
import automaton.constructor.model.module.executor.Executor
import automaton.constructor.model.module.executor.STEPPING_STRATEGIES
import automaton.constructor.utils.SettingGroupEditor
import automaton.constructor.view.memory.inputDataView
import javafx.collections.SetChangeListener
import javafx.scene.control.ScrollPane
import javafx.scene.layout.VBox
import tornadofx.*

class ExecutorView(val executor: Executor, val view: View) : VBox() {
    val controller = ExecutorController(executor, view)

    init {
        hbox {
            fitToWidth(this@ExecutorView)
            button {
                textProperty().bind(executor.startedProperty.stringBinding { if (it!!) "Stop" else "Run" })
                action {
                    controller.toggleRun()
                }
            }
            STEPPING_STRATEGIES.forEach { steppingStrategy ->
                button(steppingStrategy.name) {
                    action {
                        controller.step(steppingStrategy)
                    }
                }
            }
        }
        scrollpane {
            hbarPolicy = ScrollPane.ScrollBarPolicy.ALWAYS
            content = pane {
                add(inputDataView(executor.automaton).apply {
                    hiddenWhen(executor.startedProperty)
                    managedWhen(visibleProperty())
                })
                hbox {
                    visibleWhen(executor.startedProperty)
                    managedWhen(visibleProperty())
                    val exePathToViewMap = mutableMapOf<ExecutionPath, SettingGroupEditor>()
                    fun registerExePath(exePath: ExecutionPath) {
                        val memoryView = executionPathView(exePath)
                        exePathToViewMap[exePath] = memoryView
                        add(memoryView)
                    }
                    executor.executionPaths.forEach { registerExePath(it) }
                    executor.executionPaths.addListener(SetChangeListener {
                        if (it.wasAdded()) registerExePath(it.elementAdded)
                        if (it.wasRemoved()) children.remove(exePathToViewMap.remove(it.elementRemoved)!!)
                    })
                }
            }
        }
    }
}
