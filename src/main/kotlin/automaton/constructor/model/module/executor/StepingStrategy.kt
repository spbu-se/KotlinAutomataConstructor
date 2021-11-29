package automaton.constructor.model.module.executor

import automaton.constructor.model.Automaton
import automaton.constructor.model.module.executor.ExecutionStatus.RUNNING
import automaton.constructor.model.transition.Transition
import automaton.constructor.model.transition.property.EPSILON_VALUE

class SteppingStrategy(
    val name: String,
    val closureExtractor: (Automaton, ExecutionPath) -> Collection<ExecutionPath>,
    val transitionExtractor: (Automaton, ExecutionPath) -> Collection<Transition>
)

val STEP_BY_STATE_STRATEGY = SteppingStrategy(
    "Step by state",
    closureExtractor = { _, executionPath -> listOf(executionPath) },
    transitionExtractor = { automaton, executionPath ->
        automaton.getPossibleTransitions(executionPath.state, executionPath.memory)
    }
)

val STEP_BY_CLOSURE_STRATEGY = SteppingStrategy(
    "Step by closure",
    closureExtractor = { automaton, executionPath -> automaton.getClosure(executionPath) },
    transitionExtractor = { automaton, executionPath ->
        automaton.getPossibleTransitions(executionPath.state, executionPath.memory).filter { !it.isPure() }
    }
)

val STEPPING_STRATEGIES = listOf(STEP_BY_STATE_STRATEGY, STEP_BY_CLOSURE_STRATEGY)

fun Transition.isPure() = allProperties.all { it.value == EPSILON_VALUE }

fun Automaton.getClosure(executionPath: ExecutionPath): Collection<ExecutionPath> {
    val closure = mutableMapOf(executionPath.state to executionPath)
    val unhandledPaths = mutableListOf(executionPath)
    while (unhandledPaths.isNotEmpty()) {
        val curPath = unhandledPaths.removeLast()
        getPureTransitions(curPath.state).forEach { pureTransition ->
            if (!closure.containsKey(pureTransition.target)) {
                val fork = ExecutionPath(curPath)
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
