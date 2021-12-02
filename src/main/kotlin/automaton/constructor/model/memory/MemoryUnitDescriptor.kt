package automaton.constructor.model.memory

import automaton.constructor.model.transition.property.TransitionPropertyDescriptor
import automaton.constructor.utils.Editable

interface MemoryUnitDescriptor : Editable {
    val filters: List<TransitionPropertyDescriptor<*>>
    val sideEffects: List<TransitionPropertyDescriptor<*>>

    // if all memory units are always ready to terminate then there should be no transitions from final states
    val isAlwaysReadyToTerminate: Boolean get() = true

    // if any memory unit may require acceptance then automaton can ran without final states
    val mayRequireAcceptance: Boolean get() = false

    override var displayName: String
    fun createMemoryUnit(): MemoryUnit
}
