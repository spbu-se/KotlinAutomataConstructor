package automaton.constructor.model.memory

import automaton.constructor.model.transition.Transition
import automaton.constructor.utils.Editable

interface MemoryUnit : Editable {
    val descriptor: MemoryUnitDescriptor
    val status: MemoryUnitStatus
    override val displayName: String get() = descriptor.displayName

    fun getCurrentFilterValues(): List<*>
    fun takeTransition(transition: Transition)
    fun copy(): MemoryUnit
}

