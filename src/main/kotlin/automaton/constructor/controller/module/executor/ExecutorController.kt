package automaton.constructor.controller.module.executor

import automaton.constructor.model.module.executor.ExecutionStatus.*
import automaton.constructor.model.module.executor.Executor
import automaton.constructor.model.module.executor.SteppingStrategy
import automaton.constructor.model.module.problems
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
                ACCEPTED -> "Input accepted"
                REJECTED -> "Input rejected"
                FROZEN -> "Execution has been frozen"
                RUNNING -> "The automaton didn't halt in one second"
            }
            executor.stop()
            information("Execution result", executionResult)
        }
    }

    fun step(steppingStrategy: SteppingStrategy) {
        if (executor.started) executor.step(steppingStrategy)
        else tryStart()
    }

    private fun tryStart(): Boolean {
        if (automaton.problems.isNotEmpty()) {
            error("Execution failed", automaton.problems.joinToString("\n") { it.message })
            return false
        }
        executor.start()
        return true
    }
}
