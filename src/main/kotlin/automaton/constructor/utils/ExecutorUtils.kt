package automaton.constructor.utils

import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.memory.MemoryUnit
import automaton.constructor.model.module.executor.ExecutionStatus
import automaton.constructor.model.module.executor.Executor
import automaton.constructor.model.module.problems
import automaton.constructor.view.module.executor.executionLeafView

data class ExecutorResult(
    val executionResult: String,
    val graphic: SettingGroupEditor?
)

fun startNewExecutorOrNull(automaton: Automaton, memory: List<MemoryUnit>? = null): Executor? {
    if (automaton.problems.isNotEmpty()) {
        tornadofx.error(
            I18N.messages.getString("ExecutorController.Error.ExecutionFailed"),
            automaton.problems.joinToString("\n") { it.message },
            title = I18N.messages.getString("Dialog.error")
        )
        return null
    }
    return Executor(automaton).apply {
        if (memory == null) {
            start()
        } else {
            start(memory)
        }
    }
}

fun createExecutorAndRun(automaton: Automaton, memory: List<MemoryUnit>? = null): ExecutorResult? {
    val executor = startNewExecutorOrNull(automaton, memory)
    if (executor == null) {
        return null
    }
    executor.runFor()
    val executionResult = when (executor.status) {
        ExecutionStatus.ACCEPTED -> I18N.messages.getString("ExecutorController.Executor.Status.Accepted")
        ExecutionStatus.REJECTED -> I18N.messages.getString("ExecutorController.Executor.Status.Rejected")
        ExecutionStatus.FROZEN -> I18N.messages.getString("ExecutorController.Executor.Status.Frozen")
        ExecutionStatus.RUNNING -> I18N.messages.getString("ExecutorController.Executor.Status.Running")
    }
    val graphic = executor.acceptedExeStates.firstOrNull()?.let { executionLeafView(it) }
    automaton.clearExecutionStates() // faster analog of executor.stop()
    return ExecutorResult(executionResult, graphic)
}
