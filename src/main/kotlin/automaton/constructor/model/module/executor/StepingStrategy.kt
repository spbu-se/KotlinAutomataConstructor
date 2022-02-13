package automaton.constructor.model.module.executor

import automaton.constructor.model.Automaton
import automaton.constructor.model.module.executor.ExecutionStatus.RUNNING
import automaton.constructor.model.transition.Transition
import automaton.constructor.model.property.EPSILON_VALUE

class SteppingStrategy(
    val name: String,
    val closureExtractor: (Automaton, ExecutionState) -> Collection<ExecutionState>,
    val transitionExtractor: (Automaton, ExecutionState) -> Collection<Transition>
)

val STEP_BY_STATE_STRATEGY = SteppingStrategy(
    "Step by state",
    closureExtractor = { _, executionState -> listOf(executionState) },
    transitionExtractor = { automaton, executionState ->
        automaton.getPossibleTransitions(executionState.state, executionState.memory)
    }
)

val STEP_BY_CLOSURE_STRATEGY = SteppingStrategy(
    "Step by closure",
    closureExtractor = { automaton, executionState -> automaton.getClosure(executionState) },
    transitionExtractor = { automaton, executionState ->
        automaton.getPossibleTransitions(executionState.state, executionState.memory).filter { !it.isPure() }
    }
)

val STEPPING_STRATEGIES = listOf(STEP_BY_STATE_STRATEGY, STEP_BY_CLOSURE_STRATEGY)

fun Automaton.getClosure(executionState: ExecutionState): Collection<ExecutionState> {
    val closure = mutableMapOf(executionState.state to executionState)
    val unhandledPaths = mutableListOf(executionState)
    while (unhandledPaths.isNotEmpty()) {
        val curPath = unhandledPaths.removeLast()
        getPureTransitions(curPath.state).forEach { pureTransition ->
            if (!closure.containsKey(pureTransition.target)) {
                val fork = ExecutionState(curPath)
                fork.state = pureTransition.target
                fork.updateStatus()
                closure[pureTransition.target] = fork
                if (fork.status == RUNNING)
                    unhandledPaths.add(fork)
            }
        }
    }
    return closure.values
}
