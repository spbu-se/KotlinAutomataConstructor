package automaton.constructor.model.memory

import automaton.constructor.model.memory.MemoryUnitStatus.READY_TO_ACCEPT
import automaton.constructor.model.property.DynamicPropertyDescriptors
import automaton.constructor.model.property.EPSILON_VALUE
import automaton.constructor.model.transition.Transition
import automaton.constructor.utils.MonospaceEditableString
import automaton.constructor.utils.noPropertiesSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

private const val NAME = "Register"

@Serializable(with = RegisterDescriptorSerializer::class)
class RegisterDescriptor : MonospaceEditableString("0"), MemoryUnitDescriptor {
    val expectedValue = DynamicPropertyDescriptors.stringOrEps("Expected value", canBeDeemedEpsilon = false)
    val newValue = DynamicPropertyDescriptors.stringOrEps("New value", canBeDeemedEpsilon = false)
    override val transitionFilters = listOf(expectedValue)
    override val transitionSideEffects = listOf(newValue)
    override var displayName = NAME

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

object RegisterDescriptorSerializer : KSerializer<RegisterDescriptor> by noPropertiesSerializer(
    NAME,
    { RegisterDescriptor() }
)
