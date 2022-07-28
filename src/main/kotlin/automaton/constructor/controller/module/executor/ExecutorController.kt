package automaton.constructor.controller.module.executor

import automaton.constructor.model.module.executor.ExecutionStatus.*
import automaton.constructor.model.module.executor.Executor
import automaton.constructor.model.module.executor.SteppingStrategy
import automaton.constructor.model.module.problems
import automaton.constructor.view.module.executor.executionLeafView
import automaton.constructor.utils.I18N
import tornadofx.*

class ExecutorController(val executor: Executor, val view: View) : Controller() {
    val automaton = executor.automaton

    fun toggleRun() {
        if (executor.started) {
            executor.stop()
        } else {
            if (!tryStart()) return
            executor.runFor(1_000)
            val executionResult = when (executor.status) {
                ACCEPTED -> I18N.messages.getString("ExecutorController.Executor.Status.Accepted")
                REJECTED -> I18N.messages.getString("ExecutorController.Executor.Status.Rejected")
                FROZEN -> I18N.messages.getString("ExecutorController.Executor.Status.Frozen")
                RUNNING -> I18N.messages.getString("ExecutorController.Executor.Status.Running")
            }
            val graphic = executor.acceptedStates.firstOrNull()?.let { executionLeafView(it) }
            executor.stop()
            information(
                I18N.messages.getString("ExecutorController.ExecutionResult"),
                executionResult,
                graphic = graphic,
                title = I18N.messages.getString("Dialog.information")
            )
        }
    }

    fun step(steppingStrategy: SteppingStrategy) {
        if (executor.started) executor.step(steppingStrategy)
        else tryStart()
    }

    private fun tryStart(): Boolean {
        if (automaton.problems.isNotEmpty()) {
            error(
                I18N.messages.getString("ExecutorController.Error.ExecutionFailed"),
                automaton.problems.joinToString("\n") { it.message },
                title = I18N.messages.getString("Dialog.error")
            )
            return false
        }
        executor.start()
        return true
    }
}
