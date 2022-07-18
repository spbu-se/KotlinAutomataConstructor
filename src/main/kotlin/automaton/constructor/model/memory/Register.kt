package automaton.constructor.model.memory

import automaton.constructor.model.data.RegisterDescriptorData
import automaton.constructor.model.memory.MemoryUnitStatus.READY_TO_ACCEPT
import automaton.constructor.model.property.DynamicPropertyDescriptors
import automaton.constructor.model.property.EPSILON_VALUE
import automaton.constructor.model.transition.Transition
import automaton.constructor.utils.MonospaceEditableString
import automaton.constructor.utils.I18N.messages
import tornadofx.*

class RegisterDescriptor : MonospaceEditableString("0"), MemoryUnitDescriptor {
    val expectedValue = DynamicPropertyDescriptors.stringOrEps(
        messages.getString("Register.ExpectedValue"),
        canBeDeemedEpsilon = false
    )
    val newValue = DynamicPropertyDescriptors.stringOrEps(
        messages.getString("Register.NewValue"),
        canBeDeemedEpsilon = false
    )
    override val transitionFilters = listOf(expectedValue)
    override val transitionSideEffects = listOf(newValue)
    override var displayName: String = messages.getString("Register")

    override fun getData() = RegisterDescriptorData

    override fun createMemoryUnit() = Register(this, value)
}

class Register(
    override val descriptor: RegisterDescriptor,
    initValue: String
) : MonospaceEditableString(initValue), MemoryUnit {
    override val observableStatus = READY_TO_ACCEPT.toProperty()
    override val status: MemoryUnitStatus by observableStatus

    override fun getCurrentFilterValues() = listOf(value)

    override fun takeTransition(transition: Transition) {
        val newValue = transition[descriptor.newValue]
        if (newValue != EPSILON_VALUE) value = newValue
    }

    override fun copy() = Register(descriptor, value)
}
