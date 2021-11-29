package automaton.constructor.model.memory

import automaton.constructor.model.transition.property.TransitionPropertyDescriptor
import automaton.constructor.utils.Editable

interface MemoryUnitDescriptor : Editable {
    val filters: List<TransitionPropertyDescriptor<*>>
    val sideEffects: List<TransitionPropertyDescriptor<*>>

    // TODO val isAlwaysReadyToTerminate (if all memory units are always ready to terminate then there should be no transitions from final states)
    // TODO val mayRequireAcceptance (if any memory unit may require acceptance then automaton can ran without final states)
    override var displayName: String
    fun createMemoryUnit(): MemoryUnit
}
