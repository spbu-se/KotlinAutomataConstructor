package automaton.constructor.model.module.executor

import automaton.constructor.model.MemoryUnit
import automaton.constructor.model.MemoryUnit.Status.*
import automaton.constructor.model.State
import automaton.constructor.model.module.executor.ExecutionStatus.*
import automaton.constructor.model.transition.Transition
import tornadofx.*

class ExecutionPath private constructor(
    state: State,
    val memory: List<MemoryUnit>,
    status: ExecutionStatus
) {
    val stateProperty = state.toProperty()
    var state: State by stateProperty

    val statusProperty = status.toProperty()
    var status: ExecutionStatus by statusProperty

    constructor(initState: State, memory: List<MemoryUnit>) : this(initState, memory.map { it.copy() }, RUNNING) {
        updateStatus()
    }

    constructor(other: ExecutionPath) : this(other.state, other.memory.map { it.copy() }, other.status)

    fun takeTransition(transition: Transition) {
        state = transition.target
        memory.forEach { it.takeTransition(transition) }
        updateStatus()
    }

    private fun updateStatus() {
        if (
            (state.isFinal || memory.any { it.status == REQUIRES_ACCEPTANCE }) &&
            memory.all { it.status != NOT_READY_TO_ACCEPT }
        ) status = ACCEPTED
        else if (memory.any { it.status == REQUIRES_TERMINATION || it.status == REQUIRES_ACCEPTANCE })
            status = REJECTED
    }

    fun fail() {
        status = REJECTED
    }
}
