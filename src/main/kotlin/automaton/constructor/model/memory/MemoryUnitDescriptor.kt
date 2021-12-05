package automaton.constructor.model.memory

import automaton.constructor.model.memory.MemoryUnitStatus.NOT_READY_TO_ACCEPT
import automaton.constructor.model.memory.MemoryUnitStatus.REQUIRES_ACCEPTANCE
import automaton.constructor.model.transition.property.TransitionPropertyDescriptor
import automaton.constructor.utils.Editable

/**
 * Describes a [MemoryUnit] that each execution state of the automaton must have
 * and provides the way to set initial data of such memory unit
 */
interface MemoryUnitDescriptor : Editable {
    /**
     * A list of filters that each transition in the automaton must have
     */
    val filters: List<TransitionPropertyDescriptor<*>>

    /**
     * A list of side effects that each transition in the automaton must have
     */
    val sideEffects: List<TransitionPropertyDescriptor<*>>

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
