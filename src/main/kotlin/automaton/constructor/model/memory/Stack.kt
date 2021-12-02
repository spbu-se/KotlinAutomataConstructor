package automaton.constructor.model.memory

import automaton.constructor.model.memory.MemoryUnitStatus.*
import automaton.constructor.model.transition.Transition
import automaton.constructor.model.transition.property.EPSILON_VALUE
import automaton.constructor.model.transition.property.createCharOrEpsTransitionPropertyDescriptor
import automaton.constructor.model.transition.property.createStringOrEpsTransitionPropertyDescriptor
import automaton.constructor.utils.MonospaceEditableString
import javafx.scene.layout.VBox
import tornadofx.*

class StackDescriptor : MonospaceEditableString("z"), MemoryUnitDescriptor {
    val expectedChar = createCharOrEpsTransitionPropertyDescriptor("Expected char", canBeDeemedEpsilon = false)
    val pushedString = createStringOrEpsTransitionPropertyDescriptor("Pushed string", canBeDeemedEpsilon = false)
    override val filters = listOf(expectedChar)
    override val sideEffects = listOf(pushedString)
    override var displayName = "Stack"
    override val mayRequireAcceptance get() = true

    val acceptsByEmptyStackProperty = false.toProperty()
    var acceptsByEmptyStack by acceptsByEmptyStackProperty

    override fun createMemoryUnit() = Stack(this, value)

    override fun createEditor() = VBox().apply {
        add(super.createTextFieldEditor())
        hbox {
            checkbox { selectedProperty().bindBidirectional(acceptsByEmptyStackProperty) }
            label("Accept by empty stack")
        }
    }
}

class Stack(
    override val descriptor: StackDescriptor,
    initValue: String
) : MonospaceEditableString(initValue), MemoryUnit {
    override fun getCurrentFilterValues() = listOf(value.first())
    override val status: MemoryUnitStatus
        get() = when {
            value.isNotEmpty() -> READY_TO_ACCEPT
            descriptor.acceptsByEmptyStack -> REQUIRES_ACCEPTANCE
            else -> REQUIRES_TERMINATION
        }

    override fun takeTransition(transition: Transition) {
        var pushedString = transition[descriptor.pushedString]
        if (pushedString == EPSILON_VALUE) pushedString = ""
        value = pushedString + (if (transition[descriptor.expectedChar] == EPSILON_VALUE) value else value.drop(1))
    }

    override fun copy() = Stack(descriptor, value)
}
