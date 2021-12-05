package automaton.constructor.model.module.executor

import automaton.constructor.model.Automaton
import automaton.constructor.model.module.AutomatonModule
import automaton.constructor.model.module.executor.ExecutionStatus.*
import automaton.constructor.model.module.initialStates
import tornadofx.*

val executorFactory = { automaton: Automaton -> Executor(automaton) }
val Automaton.executor get() = getModule(executorFactory)

class Executor(val automaton: Automaton) : AutomatonModule {
    val executionStates = observableSetOf<ExecutionState>()
    var status: ExecutionStatus? = null

    val startedProperty = false.toProperty()
    var started by startedProperty
        private set

    fun start() {
        executionStates.clear()
        executionStates.addAll(
            automaton.initialStates
                .onEach { it.isCurrent = true }
                .map { initState ->
                    ExecutionState(initState, automaton.memoryDescriptors.map { it.createMemoryUnit() })
                }
        )
        status = calculateStatus()
        started = true
    }

    fun stop() {
        executionStates.onEach { it.state.isCurrent = false }.clear()
        status = null
        started = false
    }

    fun runFor(millis: Long) {
        val deadlineMillis = System.currentTimeMillis() + millis
        while (status == RUNNING && System.currentTimeMillis() < deadlineMillis)
            step(STEP_BY_CLOSURE_STRATEGY)
    }

    fun step(steppingStrategy: SteppingStrategy) {
        val forks = mutableListOf<ExecutionState>()
        val runningPaths = executionStates.filter { it.status == RUNNING }
        runningPaths.forEach { it.state.isCurrent = false }
        runningPaths.forEach { path ->
            steppingStrategy.closureExtractor(automaton, path).forEach { curPath ->
                if (curPath !== path) forks.add(curPath)
                if (curPath.status == RUNNING) {
                    val transitions = steppingStrategy.transitionExtractor(automaton, curPath)
                    if (transitions.isEmpty()) curPath.fail()
                    else {
                        transitions
                            .drop(1)
                            .forEach { transition ->
                                val fork = ExecutionState(curPath)
                                fork.takeTransition(transition)
                                forks.add(fork)
                            }
                        curPath.takeTransition(transitions.first())
                    }
                }
            }
        }
        (runningPaths + forks).filter { it.status == RUNNING }.forEach { it.state.isCurrent = true }
        executionStates.addAll(forks)
        status = calculateStatus()
    }

    private fun calculateStatus() = when {
        executionStates.any { it.status == ACCEPTED } -> ACCEPTED
        executionStates.any { it.status == RUNNING } -> RUNNING
        else -> REJECTED
    }
}
