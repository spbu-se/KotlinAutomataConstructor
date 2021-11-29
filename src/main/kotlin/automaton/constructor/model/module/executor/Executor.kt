package automaton.constructor.model.module.executor

import automaton.constructor.model.Automaton
import automaton.constructor.model.module.AutomatonModule
import automaton.constructor.model.module.executor.ExecutionStatus.*
import automaton.constructor.model.module.initialStates
import tornadofx.*

val executorFactory = { automaton: Automaton -> Executor(automaton) }
val Automaton.executor get() = getModule(executorFactory)

class Executor(val automaton: Automaton) : AutomatonModule {
    val executionPaths = observableSetOf<ExecutionPath>()
    var status: ExecutionStatus? = null

    val startedProperty = false.toProperty()
    var started by startedProperty
        private set

    fun start() {
        executionPaths.clear()
        executionPaths.addAll(
            automaton.initialStates
                .onEach { it.isCurrent = true }
                .map { initState ->
                    ExecutionPath(initState, automaton.memoryDescriptors.map { it.createMemoryUnit() })
                }
        )
        status = calculateStatus()
        started = true
    }

    fun stop() {
        executionPaths.onEach { it.state.isCurrent = false }.clear()
        status = null
        started = false
    }

    fun run() {
        while (status == RUNNING) step(STEP_BY_CLOSURE_STRATEGY)
    }

    fun step(steppingStrategy: SteppingStrategy) {
        val forks = mutableListOf<ExecutionPath>()
        val runningPaths = executionPaths.filter { it.status == RUNNING }
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
                                val fork = ExecutionPath(curPath)
                                fork.takeTransition(transition)
                                forks.add(fork)
                            }
                        curPath.takeTransition(transitions.first())
                    }
                }
            }
        }
        (runningPaths + forks).filter { it.status == RUNNING }.forEach { it.state.isCurrent = true }
        executionPaths.addAll(forks)
        status = calculateStatus()
    }

    private fun calculateStatus() = when {
        executionPaths.any { it.status == ACCEPTED } -> ACCEPTED
        executionPaths.any { it.status == RUNNING } -> RUNNING
        else -> REJECTED
    }
}
