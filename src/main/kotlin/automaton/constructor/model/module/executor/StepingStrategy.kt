package automaton.constructor.model.module.executor

import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.module.executor.ExecutionStatus.RUNNING
import automaton.constructor.model.transition.Transition

class SteppingStrategy(
    val name: String,
    private val closureExtractor: (Automaton, ExecutionState) -> Collection<ExecutionState>,
    private val transitionExtractor: (Automaton, ExecutionState) -> Collection<Transition>
) {
    fun step(automaton: Automaton, executionState: ExecutionState) {
        if (executionState.status == RUNNING)
            closureExtractor(automaton, executionState).forEach { curState ->
                if (curState.status == RUNNING) {
                    val transitions = transitionExtractor(automaton, curState)
                    if (transitions.isNotEmpty()) transitions.forEach { transition -> curState.takeTransition(transition) }
                    else if (curState.children.isEmpty()) curState.fail()
                }
            }
    }
}

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
    val unhandledStates = mutableListOf(executionState)
    while (unhandledStates.isNotEmpty()) {
        val curState = unhandledStates.removeLast()
        getPureTransitions(curState.state).forEach { pureTransition ->
            if (!closure.containsKey(pureTransition.target)) {
                val nextState = curState.takeTransition(pureTransition)
                closure[pureTransition.target] = nextState
                if (nextState.status == RUNNING)
                    unhandledStates.add(nextState)
            }
        }
    }
    return closure.values
}
