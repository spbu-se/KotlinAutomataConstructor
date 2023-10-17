package automaton.constructor.model.memory

import automaton.constructor.model.data.MemoryUnitDescriptorData
import automaton.constructor.model.memory.AcceptanceRequiringPolicy.*
import automaton.constructor.model.memory.MemoryUnitStatus.NOT_READY_TO_ACCEPT
import automaton.constructor.model.memory.MemoryUnitStatus.REQUIRES_ACCEPTANCE
import automaton.constructor.model.property.DynamicPropertyDescriptor
import automaton.constructor.utils.Editable

/**
 * Describes a [MemoryUnit] that each execution state of the automaton must have
 * and provides the way to set initial data of such memory unit
 */
interface MemoryUnitDescriptor : Editable {
    val transitionSideEffects: List<DynamicPropertyDescriptor<*>>
    val transitionFilters: List<DynamicPropertyDescriptor<*>>

    val stateSideEffects: List<DynamicPropertyDescriptor<*>> get() = emptyList()

    /**
     * `true` if the memory unit described by this descriptor can't have [NOT_READY_TO_ACCEPT] status
     * If all memory units are always ready to terminate then there should be no transitions from final states
     */
    val isAlwaysReadyToTerminate: Boolean get() = true

    val allowsStepByClosure: Boolean get() = true

    /**
     * specifies whether the memory unit described by this descriptor [requires acceptance][REQUIRES_ACCEPTANCE]
     * [always][ALWAYS], [never][NEVER], or [sometimes][SOMETIMES]
     */
    val acceptanceRequiringPolicy: AcceptanceRequiringPolicy get() = NEVER

    override var displayName: String

    fun getData(): MemoryUnitDescriptorData

    /**
     * Creates [MemoryUnit] described by this descriptor with initial data specified in editor returned by
     * [createEditor]
     */
    fun createMemoryUnit(initMemoryContent: MemoryUnitDescriptor = this): MemoryUnit

    fun copy(): MemoryUnitDescriptor = getData().createDescriptor()
}
