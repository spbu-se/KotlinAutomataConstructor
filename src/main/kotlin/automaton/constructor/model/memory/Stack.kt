package automaton.constructor.model.memory

import automaton.constructor.model.data.StackDescriptorData
import automaton.constructor.model.element.Transition
import automaton.constructor.model.memory.MemoryUnitStatus.*
import automaton.constructor.model.property.DynamicPropertyDescriptors
import automaton.constructor.model.property.EPSILON_VALUE
import automaton.constructor.utils.I18N.messages
import automaton.constructor.utils.MonospaceEditableString
import automaton.constructor.utils.nonNullObjectBinding
import javafx.beans.value.ObservableValue
import javafx.scene.layout.VBox
import tornadofx.*

class StackDescriptor(acceptsByEmptyStack: Boolean = false) : MonospaceEditableString("z"), MemoryUnitDescriptor {
    constructor(acceptsByEmptyStack: Boolean, initValue: String): this(acceptsByEmptyStack) {
        value = initValue
    }

    val expectedChar = DynamicPropertyDescriptors.charOrEps(
        messages.getString("Stack.ExpectedChar"),
        canBeDeemedEpsilon = false
    )
    val pushedValue = DynamicPropertyDescriptors.stringOrEps(
        messages.getString("Stack.PushedString"),
        canBeDeemedEpsilon = false
    )
    override val transitionFilters = listOf(expectedChar)
    override val transitionSideEffects = listOf(pushedValue)
    override var displayName: String = messages.getString("Stack")
    override val acceptanceRequiringPolicy get() = AcceptanceRequiringPolicy.SOMETIMES

    val acceptsByEmptyStackProperty = acceptsByEmptyStack.toProperty()
    var acceptsByEmptyStack by acceptsByEmptyStackProperty

    override fun getData() = StackDescriptorData(acceptsByEmptyStack, value)

    override fun createMemoryUnit(initMemoryContent: MemoryUnitDescriptor) =
        Stack(this, (initMemoryContent as StackDescriptor).value)

    override fun createEditor() = VBox().apply {
        add(super.createTextFieldEditor())
        hbox {
            checkbox { selectedProperty().bindBidirectional(acceptsByEmptyStackProperty) }
            label(messages.getString("Stack.AcceptByEmptyStack"))
        }
    }

    override fun isCompatibleWithDescriptor(descriptor: MemoryUnitDescriptor): Boolean =
        descriptor is StackDescriptor
}

class Stack(
    override val descriptor: StackDescriptor,
    initValue: String
) : MonospaceEditableString(initValue), MemoryUnit {
    override fun getCurrentFilterValues() = listOf(value.first())

    override val observableStatus: ObservableValue<MemoryUnitStatus>
        get() = valueProperty.nonNullObjectBinding(descriptor.acceptsByEmptyStackProperty) {
            when {
                value.isNotEmpty() -> READY_TO_ACCEPT
                descriptor.acceptsByEmptyStack -> REQUIRES_ACCEPTANCE
                else -> REQUIRES_TERMINATION
            }
        }
    override val status: MemoryUnitStatus by observableStatus

    override fun onTransition(transition: Transition) {
        var pushedString = transition[descriptor.pushedValue]
        if (pushedString == EPSILON_VALUE) pushedString = ""
        value = pushedString + (if (transition[descriptor.expectedChar] == EPSILON_VALUE) value else value.drop(1))
    }

    override fun copy() = Stack(descriptor, value)
}
