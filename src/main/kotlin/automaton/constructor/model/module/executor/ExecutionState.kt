package automaton.constructor.model.module.executor

import automaton.constructor.model.element.*
import automaton.constructor.model.memory.MemoryUnit
import automaton.constructor.model.memory.MemoryUnitStatus.NOT_READY_TO_ACCEPT
import automaton.constructor.model.memory.MemoryUnitStatus.REQUIRES_ACCEPTANCE
import automaton.constructor.model.module.executor.ExecutionStatus.*
import automaton.constructor.utils.nonNullObjectBinding
import javafx.beans.binding.Bindings.*
import javafx.beans.binding.BooleanBinding
import javafx.beans.value.ObservableValue
import javafx.collections.SetChangeListener
import tornadofx.*

sealed class ExecutionState(
    val vertex: AutomatonVertex,
    val lastTransition: Transition?,
    val memory: List<MemoryUnit>,
    val superState: SuperExecutionState?
) {
    val isFrozenProperty = false.toProperty()
    var isFrozen by isFrozenProperty
    val statusProperty = RUNNING.toProperty().apply {
        onChange {
            if (it == ACCEPTED || it == REJECTED) justAcceptedOrRejected = true
        }
    }
    val observableDeepStatus: ObservableValue<ExecutionStatus> =
        if (superState == null) statusProperty
        else `when`(statusProperty.isNotEqualTo(ACCEPTED)).then(statusProperty).otherwise(superState.statusProperty)
    val deepStatus: ExecutionStatus by observableDeepStatus
    val justAcceptedOrRejectedProperty = false.toProperty()
    var justAcceptedOrRejected by justAcceptedOrRejectedProperty
    val canHaveMoreChildrenProperty = true.toProperty()
    var canHaveMoreChildren by canHaveMoreChildrenProperty
    val requiresProcessingBinding: BooleanBinding = canHaveMoreChildrenProperty.or(justAcceptedOrRejectedProperty)
    val requiresProcessing by requiresProcessingBinding
    val observableText: ObservableValue<String> =
        if (superState == null) vertex.nameProperty
        else concat(superState.observableText, " -> ", vertex.nameProperty)
    val text: String by observableText
    var status: ExecutionStatus by statusProperty
    val children = observableSetOf<ExecutionState>()

    fun takeTransition(transition: Transition, memory: List<MemoryUnit> = this.memory) =
        createExecutionState(
            transition.target,
            transition,
            memory.map { it.copy().apply { onTransition(transition) } },
            superState
        ).also { children.add(it) }

    fun fail() {
        statusProperty.unbind()
        status = REJECTED
    }

    open fun collapse() {
        superState?.let { superState ->
            superState.children.forEach { it.collapse() }
            superState.children.clear()
            superState.unhandledAcceptedStates.clear()
            superState.unhandledAcceptedStates.addAll(superState.subExecutor.acceptedExeStates)
        }
        children.forEach { it.collapse() }
        children.clear()
    }

    companion object {
        fun createExecutionState(
            vertex: AutomatonVertex,
            lastTransition: Transition?,
            memory: List<MemoryUnit>,
            superState: SuperExecutionState?
        ): ExecutionState = when (vertex) {
            is State -> SimpleExecutionState(vertex, lastTransition, memory, superState)
            is HasSubAutomaton -> SuperExecutionState(vertex, lastTransition, memory, superState)
            else -> kotlin.error("Unsupported vertex type: $vertex")
        }
    }
}

class SimpleExecutionState(
    val state: State,
    lastTransition: Transition?,
    memory: List<MemoryUnit>,
    superState: SuperExecutionState?
) : ExecutionState(state, lastTransition, memory, superState) {
    init {
        memory.forEach { it.onStateEntered(state) }
        val isRecursiveSub = superState?.buildingBlock is RecursiveAutomatonBox
        statusProperty.bind(
            state.isFinalProperty.nonNullObjectBinding(
                isFrozenProperty,
                *memory.map { it.observableStatus }.toTypedArray()
            ) {
                when {
                    isRecursiveSub && state.isFinal -> ACCEPTED
                    (state.isFinal || memory.any { it.status == REQUIRES_ACCEPTANCE }) &&
                            memory.all { it.status != NOT_READY_TO_ACCEPT } -> ACCEPTED

                    isFrozen -> FROZEN
                    else -> RUNNING
                }
            })
        canHaveMoreChildrenProperty.bind(
            isEmpty(children).and(
                statusProperty.isEqualTo(RUNNING).or(statusProperty.isEqualTo(FROZEN))
            )
        )
    }
}

class SuperExecutionState(
    val buildingBlock: HasSubAutomaton,
    lastTransition: Transition?,
    memory: List<MemoryUnit>,
    superState: SuperExecutionState?
) : ExecutionState(buildingBlock as AutomatonVertex, lastTransition, memory, superState) {
    private val effectiveSubAutomaton = when (buildingBlock) {
        is RecursiveAutomatonBox -> (buildingBlock.subAutomaton as? automaton.constructor.model.automaton.RecursiveAutomaton)
            ?.shallowCloneForRecursion() ?: buildingBlock.subAutomaton

        else -> buildingBlock.subAutomaton
    }
    val subExecutor = Executor(effectiveSubAutomaton, this)
    val unhandledAcceptedStates = observableSetOf<ExecutionState>()

    init {
        statusProperty.bind(vertex.isFinalProperty.nonNullObjectBinding(isFrozenProperty, subExecutor.statusBinding) {
            when {
                vertex.isFinal && subExecutor.status == ACCEPTED -> ACCEPTED
                isFrozen || subExecutor.status == FROZEN -> FROZEN
                else -> RUNNING
            }
        })
        subExecutor.acceptedExeStates.addListener(SetChangeListener {
            if (it.wasAdded()) unhandledAcceptedStates.add(it.elementAdded)
            if (it.wasRemoved()) unhandledAcceptedStates.remove(it.elementRemoved)
        })
        canHaveMoreChildrenProperty.bind(not(subExecutor.finishedBinding).or(isNotEmpty(unhandledAcceptedStates)))
    }

    override fun collapse() {
        subExecutor.stop()
        super.collapse()
    }
}
