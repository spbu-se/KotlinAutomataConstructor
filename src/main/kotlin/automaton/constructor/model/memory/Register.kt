package automaton.constructor.model.memory

import automaton.constructor.model.data.RegisterDescriptorData
import automaton.constructor.model.element.Transition
import automaton.constructor.model.memory.MemoryUnitStatus.READY_TO_ACCEPT
import automaton.constructor.model.property.DynamicPropertyDescriptors
import automaton.constructor.model.property.EPSILON_VALUE
import automaton.constructor.utils.I18N.messages
import automaton.constructor.utils.MonospaceEditableString
import tornadofx.*

class RegisterDescriptor() : MonospaceEditableString("0"), MemoryUnitDescriptor {
    constructor(initValue: String): this() {
        value = initValue
    }

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

    override fun getData() = RegisterDescriptorData(value)

    override fun createMemoryUnit(initMemoryContent: MemoryUnitDescriptor) =
        Register(this, (initMemoryContent as RegisterDescriptor).value)
}

class Register(
    override val descriptor: RegisterDescriptor,
    initValue: String
) : MonospaceEditableString(initValue), MemoryUnit {
    override val observableStatus = READY_TO_ACCEPT.toProperty()
    override val status: MemoryUnitStatus by observableStatus

    override fun getCurrentFilterValues() = listOf(value)

    override fun onTransition(transition: Transition) {
        val newValue = transition[descriptor.newValue]
        if (newValue != EPSILON_VALUE) value = newValue
    }

    override fun copy() = Register(descriptor, value)
}
