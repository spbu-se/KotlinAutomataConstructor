package automaton.constructor.controller.module.executor.tree

import automaton.constructor.model.module.executor.ExecutionState
import automaton.constructor.model.module.executor.Executor
import automaton.constructor.model.module.executor.STEP_BY_STATE_STRATEGY

class ExecutionTreeController(val executor: Executor) {
    fun toggleExecutionState(executionState: ExecutionState) {
        if (executionState.children.isEmpty()) {
            executionState.isFrozen = false
            STEP_BY_STATE_STRATEGY.step(executor.automaton, executionState)
        } else executionState.collapse()
    }
}
