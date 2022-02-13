package automaton.constructor.model.memory

import automaton.constructor.model.memory.MemoryUnitStatus.NOT_READY_TO_ACCEPT
import automaton.constructor.model.memory.MemoryUnitStatus.REQUIRES_ACCEPTANCE
import automaton.constructor.model.property.DynamicPropertyDescriptor
import automaton.constructor.model.property.DynamicPropertyDescriptorGroup
import automaton.constructor.utils.Editable

/**
 * Describes a [MemoryUnit] that each execution state of the automaton must have
 * and provides the way to set initial data of such memory unit
 */
interface MemoryUnitDescriptor : Editable {
    val transitionSideEffects: List<DynamicPropertyDescriptor<*>>
    val transitionFilters: List<DynamicPropertyDescriptor<*>>

    val stateSideEffects: List<DynamicPropertyDescriptor<*>> get() = emptyList()
    val stateFilters: List<DynamicPropertyDescriptor<*>> get() = emptyList()

    /**
     * `true` if the memory unit described by this descriptor can't have [NOT_READY_TO_ACCEPT] status
     * If all memory units are always ready to terminate then there should be no transitions from final states
     */
    val isAlwaysReadyToTerminate: Boolean get() = true

    /**
     * `true` if the memory unit described by this descriptor can have [REQUIRES_ACCEPTANCE] status
     * If any memory unit may require acceptance then automaton can ran without final states
     */
    val mayRequireAcceptance: Boolean get() = false

    override var displayName: String

    /**
     * Creates [MemoryUnit] described by this descriptor with initial data specified in editor returned by
     * [createEditor]
     */
    fun createMemoryUnit(): MemoryUnit
}
