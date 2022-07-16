package automaton.constructor.model.memory

import automaton.constructor.model.memory.MemoryUnitStatus.*
import automaton.constructor.model.property.DynamicPropertyDescriptors
import automaton.constructor.model.property.EPSILON_VALUE
import automaton.constructor.model.transition.Transition
import automaton.constructor.utils.MonospaceEditableString
import automaton.constructor.utils.MostlyGeneratedOrInline
import automaton.constructor.utils.nonNullObjectBinding
import automaton.constructor.utils.surrogateSerializer
import automaton.constructor.utils.I18N.labels
import javafx.beans.value.ObservableValue
import javafx.scene.layout.VBox
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import tornadofx.*

private val NAME: String = labels.getString("Stack.NAME")

@Serializable(with = StackDescriptorSerializer::class)
class StackDescriptor(acceptsByEmptyStack: Boolean = false) : MonospaceEditableString("z"), MemoryUnitDescriptor {
    val expectedChar = DynamicPropertyDescriptors.charOrEps(
        labels.getString("Stack.StackDescriptor.ExpectedChar"),
        canBeDeemedEpsilon = false
    )
    val pushedString = DynamicPropertyDescriptors.stringOrEps(
        labels.getString("Stack.StackDescriptor.PushedString"),
        canBeDeemedEpsilon = false
    )
    override val transitionFilters = listOf(expectedChar)
    override val transitionSideEffects = listOf(pushedString)
    override var displayName = NAME
    override val mayRequireAcceptance get() = true

    val acceptsByEmptyStackProperty = acceptsByEmptyStack.toProperty()
    var acceptsByEmptyStack by acceptsByEmptyStackProperty

    override fun createMemoryUnit() = Stack(this, value)

    override fun createEditor() = VBox().apply {
        add(super.createTextFieldEditor())
        hbox {
            checkbox { selectedProperty().bindBidirectional(acceptsByEmptyStackProperty) }
            label(labels.getString("Stack.StackDescriptor.AcceptByEmptyStack"))
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

@Serializable
@SerialName("Stack")
@MostlyGeneratedOrInline
data class StackDescriptorData(val acceptsByEmptyStack: Boolean)

object StackDescriptorSerializer : KSerializer<StackDescriptor> by surrogateSerializer(
    { StackDescriptorData(it.acceptsByEmptyStack) },
    { StackDescriptor(it.acceptsByEmptyStack) }
)
