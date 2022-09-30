package automaton.constructor.view.module.executor

import automaton.constructor.controller.module.executor.ExecutorController
import automaton.constructor.model.module.executor.*
import automaton.constructor.utils.I18N.messages
import automaton.constructor.utils.SettingGroupEditor
import automaton.constructor.utils.filteredSet
import automaton.constructor.view.memory.inputDataView
import javafx.beans.binding.Bindings.not
import javafx.collections.SetChangeListener
import javafx.scene.control.ScrollPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import tornadofx.*

class ExecutorView(val executor: Executor) : HBox() {
    val controller = ExecutorController(executor)

    init {
        vbox {
            minWidth = USE_PREF_SIZE
            button {
                maxWidth = Double.MAX_VALUE
                maxHeight = Double.MAX_VALUE
                hgrow = Priority.ALWAYS
                vgrow = Priority.ALWAYS
                textProperty().bind(executor.startedBinding.stringBinding {
                    if (it!!)
                        messages.getString("ExecutorView.Stop")
                    else messages.getString("ExecutorView.Run")
                })
                action {
                    controller.toggleRun()
                }
            }
            STEPPING_STRATEGIES.filter { it.isAvailableFor(executor.automaton) }.forEach { strategy ->
                button(strategy.name) {
                    maxWidth = Double.MAX_VALUE
                    maxHeight = Double.MAX_VALUE
                    hgrow = Priority.ALWAYS
                    vgrow = Priority.ALWAYS
                    action {
                        controller.step(strategy)
                    }
                }
            }
        }
        scrollpane {
            maxWidth = Double.MAX_VALUE
            hgrow = Priority.SOMETIMES
            isFitToHeight = true
            hbarPolicy = ScrollPane.ScrollBarPolicy.ALWAYS
            content = pane {
                inputDataView(executor.automaton) {
                    hiddenWhen(executor.startedBinding)
                    managedWhen(visibleProperty())
                    fitToParentHeight()
                }
                hbox {
                    visibleWhen(executor.startedBinding)
                    managedWhen(visibleProperty())
                    fitToParentHeight()
                    val exeStateToViewMap = mutableMapOf<ExecutionState, SettingGroupEditor>()
                    fun registerExeState(exeState: ExecutionState) {
                        val memoryView = executionLeafView(exeState)
                        exeStateToViewMap[exeState] = memoryView
                        add(memoryView)
                        memoryView.fitToParentHeight()
                    }

                    val displayedExeStates = executor.flattenedRequiringProcessingExeStates.filteredSet {
                        when (it) {
                            is SimpleExecutionState -> true.toProperty()
                            is SuperExecutionState -> not(it.subExecutor.startedBinding)
                        }
                    }
                    displayedExeStates.forEach { registerExeState(it) }
                    displayedExeStates.addListener(SetChangeListener {
                        if (it.wasAdded()) registerExeState(it.elementAdded)
                        if (it.wasRemoved()) children.remove(exeStateToViewMap.remove(it.elementRemoved)!!)
                    })
                }
            }
        }
    }
}
