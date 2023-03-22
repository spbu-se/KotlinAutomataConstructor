package automaton.constructor.controller.module.executor

import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.module.executor.ExecutionStatus.*
import automaton.constructor.model.module.executor.Executor
import automaton.constructor.model.module.executor.SteppingStrategy
import automaton.constructor.model.module.problems
import automaton.constructor.utils.I18N
import automaton.constructor.view.module.executor.executionLeafView
import tornadofx.*

class ExecutorController(viewedAutomaton: Automaton) : Controller() {
    val selectedAutomatonProperty = viewedAutomaton.toProperty()
    val selectedAutomaton: Automaton by selectedAutomatonProperty
    val debuggingExecutorProperty = null.toProperty<Executor>()
    var debuggingExecutor: Executor? by debuggingExecutorProperty

    fun toggleRun() {
        debuggingExecutor?.let {
            it.automaton.clearExecutionStates() // faster analog of executor.stop()
            debuggingExecutor = null
        } ?: run {
            val executor = startNewExecutorOrNull() ?: return@toggleRun
            executor.runFor()
            val executionResult = when (executor.status) {
                ACCEPTED -> I18N.messages.getString("ExecutorController.Executor.Status.Accepted")
                REJECTED -> I18N.messages.getString("ExecutorController.Executor.Status.Rejected")
                FROZEN -> I18N.messages.getString("ExecutorController.Executor.Status.Frozen")
                RUNNING -> I18N.messages.getString("ExecutorController.Executor.Status.Running")
            }
            val graphic = executor.acceptedExeStates.firstOrNull()?.let { executionLeafView(it) }
            executor.automaton.clearExecutionStates() // faster analog of executor.stop()
            information(
                I18N.messages.getString("ExecutorController.ExecutionResult"),
                executionResult,
                graphic = graphic,
                title = I18N.messages.getString("Dialog.information")
            )
        }
    }

    fun step(strategy: SteppingStrategy) = debuggingExecutor?.step(strategy) ?: run {
        debuggingExecutor = startNewExecutorOrNull()
    }

    private fun startNewExecutorOrNull(): Executor? {
        if (selectedAutomaton.problems.isNotEmpty()) {
            error(
                I18N.messages.getString("ExecutorController.Error.ExecutionFailed"),
                selectedAutomaton.problems.joinToString("\n") { it.message },
                title = I18N.messages.getString("Dialog.error")
            )
            return null
        }
        return Executor(selectedAutomaton).apply { start() }
    }
}
