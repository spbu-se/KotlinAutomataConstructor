package automaton.constructor.model.module.executor

import automaton.constructor.model.State
import automaton.constructor.model.memory.MemoryUnit
import automaton.constructor.model.memory.MemoryUnitStatus.NOT_READY_TO_ACCEPT
import automaton.constructor.model.memory.MemoryUnitStatus.REQUIRES_ACCEPTANCE
import automaton.constructor.model.module.executor.ExecutionStatus.*
import automaton.constructor.model.transition.Transition
import automaton.constructor.utils.nonNullObjectBinding
import tornadofx.*

class ExecutionState(
    val state: State,
    val lastTransition: Transition?,
    val memory: List<MemoryUnit>
) {
    val isFrozenProperty = false.toProperty()
    var isFrozen by isFrozenProperty
    val statusProperty = RUNNING.toProperty().apply {
        bind(
            state.isFinalProperty.nonNullObjectBinding(
                isFrozenProperty,
                *memory.map { it.observableStatus }.toTypedArray()
            ) {
                when {
                    (state.isFinal || memory.any { it.status == REQUIRES_ACCEPTANCE }) &&
                            memory.all { it.status != NOT_READY_TO_ACCEPT } -> ACCEPTED
                    isFrozen -> FROZEN
                    else -> RUNNING
                }
            })
    }
    var status: ExecutionStatus by statusProperty
    val children = observableSetOf<ExecutionState>()

    fun takeTransition(transition: Transition) =
        ExecutionState(
            transition.target,
            transition,
            memory.map { it.copy().apply { takeTransition(transition) } }
        ).also { children.add(it) }

    fun fail() {
        statusProperty.unbind()
        status = REJECTED
    }

    fun collapse() = children.clear()
}
