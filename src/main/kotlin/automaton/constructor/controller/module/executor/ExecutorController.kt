package automaton.constructor.controller.module.executor

import automaton.constructor.model.module.executor.SteppingStrategy
import automaton.constructor.model.module.problems
import automaton.constructor.view.module.executor.ExecutorView
import tornadofx.*

class ExecutorController(val executorView: ExecutorView, val view: View) : Controller() {
    val executor = executorView.executor
    val automaton = executor.automaton

    fun toggleRun() {
        if (executor.started) {
            executor.stop()
        } else {
            if (!tryStart()) return
            executor.run()
            val executionResult = executor.status
            executor.stop()
            view.dialog("Execution result") {
                label("Input ${executionResult!!.text.toLowerCase()}")
            }
        }
    }

    fun step(steppingStrategy: SteppingStrategy) {
        if (executor.started) executor.step(steppingStrategy)
        else tryStart()
    }

    private fun tryStart(): Boolean {
        if (automaton.problems.isNotEmpty()) {
            view.dialog("Execution failed") {
                label(automaton.problems.joinToString("\n") { it.message })
            }
            return false
        }
        executor.start()
        return true
    }
}
