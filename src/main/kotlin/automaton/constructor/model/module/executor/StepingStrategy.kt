package automaton.constructor.model.module.executor

import automaton.constructor.model.Automaton
import automaton.constructor.model.State
import automaton.constructor.model.transition.Transition

class SteppingStrategy(
    val name: String,
    val transitionExtractor: (automaton: Automaton, executionPath: ExecutionPath) -> Set<Transition>
)

val STEP_BY_STATE_STRATEGY = SteppingStrategy("Step by state") { automaton, exePath ->
    automaton.getPossibleTransitions(exePath.state, exePath.memory)
}

val STEP_BY_CLOSURE_STRATEGY = SteppingStrategy("Step by closure") { automaton, exePath ->
    automaton.getClosure(exePath.state).flatMapTo(mutableSetOf()) { curState ->
        automaton.getPossibleTransitions(curState, exePath.memory).filter { !it.isPure() }
    }
}

val STEPPING_STRATEGIES = listOf(STEP_BY_STATE_STRATEGY, STEP_BY_CLOSURE_STRATEGY)

fun Automaton.getClosure(state: State): Set<State> {
    val closure = mutableSetOf(state)
    val unhandledStates = mutableListOf(state)
    while (unhandledStates.isNotEmpty()) {
        val curState = unhandledStates.removeLast()
        getPureTransitions(curState).forEach { pureTransition ->
            if (closure.add(pureTransition.target))
                unhandledStates.add(pureTransition.target)
        }
    }
    return closure
}
