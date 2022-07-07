package automaton.constructor.model.module.executor

import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.module.AutomatonModule
import automaton.constructor.model.module.executor.ExecutionStatus.*
import automaton.constructor.model.module.initialStates
import automaton.constructor.utils.filteredSet
import javafx.beans.binding.Binding
import javafx.beans.binding.Bindings.*
import javafx.beans.binding.BooleanBinding
import javafx.collections.SetChangeListener
import tornadofx.*

val executorFactory = { automaton: Automaton -> Executor(automaton) }
val Automaton.executor get() = getModule(executorFactory)

class Executor(val automaton: Automaton) : AutomatonModule {
    private val childrenChangeListener: SetChangeListener<ExecutionState> = SetChangeListener<ExecutionState> {
        if (it.wasAdded()) executionStates.add(it.elementAdded)
        if (it.wasRemoved()) executionStates.remove(it.elementRemoved)
    }

    val roots = observableSetOf<ExecutionState>().apply { addListener(childrenChangeListener) }

    val executionStates = observableSetOf<ExecutionState>().apply {
        addListener(SetChangeListener { change ->
            if (change.wasAdded())
                change.elementAdded.children.onEach { add(it) }.addListener(childrenChangeListener)
            if (change.wasRemoved())
                change.elementRemoved.children.onEach { remove(it) }.removeListener(childrenChangeListener)
        })
    }

    val leafExecutionStates = executionStates.filteredSet { isEmpty(it.children) }.apply {
        addListener(SetChangeListener {
            if (it.wasAdded()) it.elementAdded.state.executionStates.add(it.elementAdded)
            if (it.wasRemoved()) it.elementRemoved.state.executionStates.remove(it.elementRemoved)
        })
    }
    val acceptedStates = leafExecutionStates.filteredSet { it.statusProperty.isEqualTo(ACCEPTED) }
    val frozenStates = leafExecutionStates.filteredSet { it.statusProperty.isEqualTo(FROZEN) }
    val activeExecutionStates = leafExecutionStates.filteredSet { it.statusProperty.isEqualTo(RUNNING) }

    val statusBinding: Binding<ExecutionStatus> =
        `when`(isNotEmpty(acceptedStates)).then(ACCEPTED).otherwise(
            `when`(isNotEmpty(activeExecutionStates)).then(RUNNING).otherwise(
                `when`(isNotEmpty(frozenStates)).then(FROZEN).otherwise(REJECTED)
            )
        )
    val status: ExecutionStatus by statusBinding

    val startedBinding: BooleanBinding = isNotEmpty(roots)
    val started by startedBinding

    fun start() {
        stop()
        roots.addAll(automaton.initialStates.map { root ->
            ExecutionState(root, null, automaton.memoryDescriptors.map { it.createMemoryUnit() })
        })
    }

    fun stop() = roots.clear()

    fun runFor(millis: Long, steppingStrategy: SteppingStrategy = STEP_BY_CLOSURE_STRATEGY) {
        val deadlineMillis = System.currentTimeMillis() + millis
        while (status == RUNNING && System.currentTimeMillis() < deadlineMillis)
            step(steppingStrategy)
    }

    fun step(steppingStrategy: SteppingStrategy) =
        leafExecutionStates.toList() // `toList()` avoids ConcurrentModification
            .forEach { steppingStrategy.step(automaton, it) }
}
