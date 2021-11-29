package automaton.constructor.model.module.executor

import automaton.constructor.model.State
import automaton.constructor.model.memory.MemoryUnit
import automaton.constructor.model.memory.MemoryUnitStatus.NOT_READY_TO_ACCEPT
import automaton.constructor.model.memory.MemoryUnitStatus.REQUIRES_ACCEPTANCE
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

    constructor(initState: State, memory: List<MemoryUnit>) : this(initState, memory, RUNNING) {
        updateStatus()
    }

    constructor(other: ExecutionPath) : this(other.state, other.memory.map { it.copy() }, other.status)

    fun takeTransition(transition: Transition) {
        state = transition.target
        memory.forEach { it.takeTransition(transition) }
        updateStatus()
    }

    fun updateStatus() {
        if ((state.isFinal || memory.any { it.status == REQUIRES_ACCEPTANCE }) &&
            memory.all { it.status != NOT_READY_TO_ACCEPT }
        ) status = ACCEPTED
    }

    fun fail() {
        status = REJECTED
    }
}
