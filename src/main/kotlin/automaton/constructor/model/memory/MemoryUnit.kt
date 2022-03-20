package automaton.constructor.model.memory

import automaton.constructor.model.property.DynamicPropertyGroup
import automaton.constructor.model.property.EPSILON_VALUE
import automaton.constructor.model.transition.Transition
import automaton.constructor.utils.Editable
import javafx.beans.value.ObservableValue

/**
 * One of the memory units belonging to some execution state of the automaton
 */
interface MemoryUnit : Editable {
    val descriptor: MemoryUnitDescriptor

    /**
     * @see MemoryUnitStatus
     */
    val observableStatus: ObservableValue<MemoryUnitStatus>
    val status: MemoryUnitStatus

    /**
     * @see MemoryUnitDescriptor.displayName
     */
    override val displayName: String get() = descriptor.displayName

    /**
     * [Transition]-s that have current filter values (or [EPSILON_VALUE]) in the filters of their
     * [DynamicPropertyGroup] corresponding to this memory unit [descriptor] can be taken by the executor
     */
    fun getCurrentFilterValues(): List<*>

    /**
     * Modifies this memory unit data according to properties of the given [transition]
     */
    fun takeTransition(transition: Transition)

    /**
     * Creates a copy of this memory unit that has the same [descriptor] and a copy of this memory unit data
     */
    fun copy(): MemoryUnit
}

