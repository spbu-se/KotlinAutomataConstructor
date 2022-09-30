package automaton.constructor.model.module.executor

import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.memory.MemoryUnit
import automaton.constructor.model.module.AutomatonModule
import automaton.constructor.model.module.executor.ExecutionStatus.*
import automaton.constructor.model.module.initialVertices
import automaton.constructor.utils.filteredSet
import javafx.beans.binding.Binding
import javafx.beans.binding.Bindings.*
import javafx.beans.binding.BooleanBinding
import javafx.collections.SetChangeListener
import tornadofx.*

private val executorFactory = { automaton: Automaton -> Executor(automaton) }
val Automaton.executor get() = getModule(executorFactory)

class Executor(val automaton: Automaton, val superState: SuperExecutionState? = null) : AutomatonModule {
    private val childrenChangeListener: SetChangeListener<ExecutionState> = SetChangeListener<ExecutionState> {
        if (it.wasAdded()) exeStates.add(it.elementAdded)
        if (it.wasRemoved()) exeStates.remove(it.elementRemoved)
    }

    val roots = observableSetOf<ExecutionState>().apply { addListener(childrenChangeListener) }

    val exeStates = observableSetOf<ExecutionState>().apply {
        addListener(SetChangeListener { change ->
            if (change.wasAdded())
                change.elementAdded.children.onEach { add(it) }.addListener(childrenChangeListener)
            if (change.wasRemoved())
                change.elementRemoved.children.onEach { remove(it) }.removeListener(childrenChangeListener)
        })
    }

    val acceptedExeStates = exeStates.filteredSet { it.statusProperty.isEqualTo(ACCEPTED) }
    val frozenExeStates = exeStates.filteredSet { it.statusProperty.isEqualTo(FROZEN) }

    val requiringProcessingExeStatesChangeListener: SetChangeListener<ExecutionState> =
        SetChangeListener<ExecutionState> {
            if (it.wasAdded()) flattenedRequiringProcessingExeStates.add(it.elementAdded)
            if (it.wasRemoved()) flattenedRequiringProcessingExeStates.remove(it.elementRemoved)
        }

    val requiringProcessingExeStates = exeStates.filteredSet { exeState ->
        exeState.requiresProcessingBinding

    }.apply {
        addListener(SetChangeListener {
            if (it.wasAdded()) it.elementAdded.vertex.executionStates.add(it.elementAdded)
            if (it.wasRemoved()) it.elementRemoved.vertex.executionStates.remove(it.elementRemoved)
        })
        addListener(requiringProcessingExeStatesChangeListener)
    }

    val flattenedRequiringProcessingExeStates = observableSetOf<ExecutionState>().apply {
        addListener(SetChangeListener { change ->
            if (change.wasAdded())
                (change.elementAdded as? SuperExecutionState)?.subExecutor?.requiringProcessingExeStates
                    ?.onEach { add(it) }?.addListener(requiringProcessingExeStatesChangeListener)
            if (change.wasRemoved())
                (change.elementAdded as? SuperExecutionState)?.subExecutor?.requiringProcessingExeStates
                    ?.onEach { remove(it) }?.removeListener(requiringProcessingExeStatesChangeListener)
        })
    }

    val requiringProcessingNonFrozenExeStates = exeStates.filteredSet {
        it.requiresProcessingBinding.and(
            it.statusProperty.isNotEqualTo(FROZEN)
        )
    }

    val statusBinding: Binding<ExecutionStatus> =
        `when`(isNotEmpty(acceptedExeStates)).then(ACCEPTED).otherwise(
            `when`(isNotEmpty(requiringProcessingNonFrozenExeStates)).then(RUNNING).otherwise(
                `when`(isNotEmpty(frozenExeStates))
                    .then(FROZEN).otherwise(REJECTED)
            )
        )
    val status: ExecutionStatus by statusBinding

    val startedBinding: BooleanBinding = isNotEmpty(roots)
    val started by startedBinding

    val finishedBinding: BooleanBinding = startedBinding.and(isEmpty(requiringProcessingExeStates))
    val finished by finishedBinding

    fun start(memory: List<MemoryUnit> = automaton.memoryDescriptors.map { it.createMemoryUnit() }) {
        stop()
        roots.addAll(automaton.initialVertices.map { root ->
            ExecutionState.create(root, null, memory.map { it.copy() }, superState)
        })
    }

    fun stop() {
        roots.clear()
        automaton.clearExecutionStates()
        flattenedRequiringProcessingExeStates.clear()
    }

    fun runFor(
        maxMillis: Long = 1000L,
        strategy: SteppingStrategy = StepByClosureStrategy,
        terminationCondition: (Executor) -> Boolean = { it.status != RUNNING }
    ) {
        val deadlineMillis = System.currentTimeMillis() + maxMillis
        while (!terminationCondition(this) && System.currentTimeMillis() < deadlineMillis)
            step(strategy)
    }

    fun step(steppingStrategy: SteppingStrategy) =
        requiringProcessingNonFrozenExeStates.toList() // `toList()` avoids ConcurrentModification
            .forEach { exeState -> steppingStrategy.step(automaton, exeState) }
}
