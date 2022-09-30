package automaton.constructor.controller.module.executor.tree

import automaton.constructor.model.module.executor.ExecutionState
import automaton.constructor.model.module.executor.Executor
import automaton.constructor.model.module.executor.StepByStateStrategy

class ExecutionTreeController(val executor: Executor) {
    fun toggleExecutionState(executionState: ExecutionState) {
        if (executionState.children.isEmpty()) {
            executionState.isFrozen = false
            StepByStateStrategy.step(executor.automaton, executionState)
        } else executionState.collapse()
    }
}
