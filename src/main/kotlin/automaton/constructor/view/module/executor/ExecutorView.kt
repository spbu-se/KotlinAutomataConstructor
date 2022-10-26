package automaton.constructor.view.module.executor

import automaton.constructor.controller.module.executor.ExecutorController
import automaton.constructor.model.module.executor.ExecutionState
import automaton.constructor.model.module.executor.STEPPING_STRATEGIES
import automaton.constructor.model.module.executor.SimpleExecutionState
import automaton.constructor.model.module.executor.SuperExecutionState
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

class ExecutorView(val controller: ExecutorController) : HBox() {
    init {
        vbox {
            minWidth = USE_PREF_SIZE
            button {
                maxWidth = Double.MAX_VALUE
                maxHeight = Double.MAX_VALUE
                hgrow = Priority.ALWAYS
                vgrow = Priority.ALWAYS
                textProperty().bind(controller.debuggingExecutorProperty.stringBinding {
                    if (it != null)
                        messages.getString("ExecutorView.Stop")
                    else messages.getString("ExecutorView.Run")
                })
                action {
                    controller.toggleRun()
                }
            }
            STEPPING_STRATEGIES.filter { it.isAvailableFor(controller.selectedAutomaton) }.forEach { strategy ->
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
                inputDataView(controller.selectedAutomaton) {
                    visibleWhen(controller.debuggingExecutorProperty.isNull)
                    managedWhen(visibleProperty())
                    fitToParentHeight()
                }
                hbox {
                    visibleWhen(controller.debuggingExecutorProperty.isNotNull)
                    managedWhen(visibleProperty())
                    fitToParentHeight()
                    controller.debuggingExecutorProperty.onChange { executor ->
                        this@hbox.clear()
                        if (executor != null) {
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
    }
}
