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

fun getNewExecutorOrNull(automaton: Automaton): Executor? {
    if (automaton.problems.isNotEmpty()) {
        tornadofx.error(
            I18N.messages.getString("ExecutorController.Error.ExecutionFailed"),
            automaton.problems.joinToString("\n") { it.message },
            title = I18N.messages.getString("Dialog.error")
        )
        return null
    }
    return Executor(automaton)
}

fun createExecutorAndRun(automaton: Automaton, memory: List<MemoryUnit>? = null): ExecutorResult? {
    val executor = getNewExecutorOrNull(automaton)
    if (executor == null) {
        return null
    }
    if (memory == null) {
        executor.start()
    } else {
        executor.start(memory)
    }
    executor.runFor()
    val executionResult = when (executor.status) {
        ExecutionStatus.ACCEPTED -> I18N.messages.getString("ExecutorController.Executor.Status.Accepted")
        ExecutionStatus.REJECTED -> I18N.messages.getString("ExecutorController.Executor.Status.Rejected")
        ExecutionStatus.FROZEN -> I18N.messages.getString("ExecutorController.Executor.Status.Frozen")
        ExecutionStatus.RUNNING -> I18N.messages.getString("ExecutorController.Executor.Status.Running")
    }
    val graphic = executor.acceptedExeStates.firstOrNull()?.let { executionLeafView(it) }
    automaton.clearExecutionStates()
    return ExecutorResult(executionResult, graphic)
}
