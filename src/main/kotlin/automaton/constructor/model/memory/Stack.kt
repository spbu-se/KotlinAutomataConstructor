package automaton.constructor.model.memory

import automaton.constructor.model.data.StackDescriptorData
import automaton.constructor.model.memory.MemoryUnitStatus.*
import automaton.constructor.model.property.DynamicPropertyDescriptors
import automaton.constructor.model.property.EPSILON_VALUE
import automaton.constructor.model.transition.Transition
import automaton.constructor.utils.MonospaceEditableString
import automaton.constructor.utils.nonNullObjectBinding
import automaton.constructor.utils.I18N.messages
import javafx.beans.value.ObservableValue
import javafx.scene.layout.VBox
import tornadofx.*

class StackDescriptor(acceptsByEmptyStack: Boolean = false) : MonospaceEditableString("z"), MemoryUnitDescriptor {
    val expectedChar = DynamicPropertyDescriptors.charOrEps(
        messages.getString("Stack.ExpectedChar"),
        canBeDeemedEpsilon = false
    )
    val pushedString = DynamicPropertyDescriptors.stringOrEps(
        messages.getString("Stack.PushedString"),
        canBeDeemedEpsilon = false
    )
    override val transitionFilters = listOf(expectedChar)
    override val transitionSideEffects = listOf(pushedString)
    override var displayName: String = messages.getString("Stack")
    override val mayRequireAcceptance get() = true

    val acceptsByEmptyStackProperty = acceptsByEmptyStack.toProperty()
    var acceptsByEmptyStack by acceptsByEmptyStackProperty

    override fun getData() = StackDescriptorData(acceptsByEmptyStack)

    override fun createMemoryUnit() = Stack(this, value)

    override fun createEditor() = VBox().apply {
        add(super.createTextFieldEditor())
        hbox {
            checkbox { selectedProperty().bindBidirectional(acceptsByEmptyStackProperty) }
            label(messages.getString("Stack.AcceptByEmptyStack"))
        }
    }
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

    override fun takeTransition(transition: Transition) {
        var pushedString = transition[descriptor.pushedString]
        if (pushedString == EPSILON_VALUE) pushedString = ""
        value = pushedString + (if (transition[descriptor.expectedChar] == EPSILON_VALUE) value else value.drop(1))
    }

    override fun copy() = Stack(descriptor, value)
}
