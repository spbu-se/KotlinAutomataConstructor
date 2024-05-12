package automaton.constructor.controller.module.executor

import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.module.executor.Executor
import automaton.constructor.model.module.executor.SteppingStrategy
import automaton.constructor.utils.I18N
import automaton.constructor.utils.createExecutorAndRun
import automaton.constructor.utils.startNewExecutorOrNull
import tornadofx.*

class ExecutorController(viewedAutomaton: Automaton, private val uiComponent: UIComponent) : Controller() {
    val selectedAutomatonProperty = viewedAutomaton.toProperty()
    val selectedAutomaton: Automaton by selectedAutomatonProperty
    val debuggingExecutorProperty = null.toProperty<Executor>()
    var debuggingExecutor: Executor? by debuggingExecutorProperty

    fun toggleRun() {
        debuggingExecutor?.let {
            it.automaton.clearExecutionStates() // faster analog of executor.stop()
            debuggingExecutor = null
        } ?: run {
            val executorResult = createExecutorAndRun(selectedAutomaton)
            if (executorResult != null) {
                information(
                    I18N.messages.getString("ExecutorController.ExecutionResult"),
                    executorResult.executionResult,
                    graphic = executorResult.graphic,
                    title = I18N.messages.getString("Dialog.information"),
                    owner = uiComponent.currentWindow
                )
            }
        }
    }

    fun step(strategy: SteppingStrategy) = debuggingExecutor?.step(strategy) ?: run {
        debuggingExecutor = startNewExecutorOrNull(selectedAutomaton)
    }
}
