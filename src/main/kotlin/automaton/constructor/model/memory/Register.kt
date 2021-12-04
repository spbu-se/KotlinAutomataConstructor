package automaton.constructor.model.memory

import automaton.constructor.model.memory.MemoryUnitStatus.READY_TO_ACCEPT
import automaton.constructor.model.transition.Transition
import automaton.constructor.model.transition.property.EPSILON_VALUE
import automaton.constructor.model.transition.property.createStringOrEpsTransitionPropertyDescriptor
import automaton.constructor.utils.MonospaceEditableString

class RegisterDescriptor : MonospaceEditableString("0"), MemoryUnitDescriptor {
    val expectedValue = createStringOrEpsTransitionPropertyDescriptor("Expected value", canBeDeemedEpsilon = false)
    val newValue = createStringOrEpsTransitionPropertyDescriptor("New value", canBeDeemedEpsilon = false)
    override val filters = listOf(expectedValue)
    override val sideEffects = listOf(newValue)
    override var displayName = "Register"

    override fun createMemoryUnit() = Register(this, value)
}

class Register(
    override val descriptor: RegisterDescriptor,
    initValue: String
) : MonospaceEditableString(initValue), MemoryUnit {
    override val status get() = READY_TO_ACCEPT

    override fun getCurrentFilterValues() = listOf(value)

    override fun takeTransition(transition: Transition) {
        val newValue = transition[descriptor.newValue]
        if (newValue != EPSILON_VALUE) value = newValue
    }

    override fun copy() = Register(descriptor, value)
}
