package automaton.constructor.model.memory.tape

import automaton.constructor.model.data.InputTapeDescriptorData
import automaton.constructor.model.memory.MemoryUnitDescriptor
import automaton.constructor.model.memory.MemoryUnitStatus
import automaton.constructor.model.memory.MemoryUnitStatus.NOT_READY_TO_ACCEPT
import automaton.constructor.model.memory.MemoryUnitStatus.REQUIRES_TERMINATION
import automaton.constructor.model.property.DynamicPropertyDescriptor
import automaton.constructor.model.property.DynamicPropertyDescriptors
import automaton.constructor.model.property.DynamicPropertyDescriptors.BLANK_CHAR
import automaton.constructor.model.property.EPSILON_VALUE
import automaton.constructor.model.transition.Transition
import automaton.constructor.utils.MonospaceEditableString
import javafx.beans.binding.Bindings.`when`
import javafx.beans.value.ObservableValue
import tornadofx.*

private const val NAME = "Input tape"

class InputTapeDescriptor : MonospaceEditableString(), MemoryUnitDescriptor {
    val expectedChar = DynamicPropertyDescriptors.charOrEps("Expected char", canBeDeemedEpsilon = true)

    override val transitionFilters = listOf(expectedChar)
    override val transitionSideEffects = emptyList<DynamicPropertyDescriptor<*>>()
    override var displayName = NAME
    override val isAlwaysReadyToTerminate get() = false

    override fun getData() = InputTapeDescriptorData

    override fun createMemoryUnit() = InputTape(this, Track(value))
}

class InputTape(
    override val descriptor: InputTapeDescriptor,
    val track: Track
) : AbstractTape(listOf(track)) {
    override val observableStatus: ObservableValue<MemoryUnitStatus> =
        `when`(track.currentProperty.isEqualTo(BLANK_CHAR))
            .then(REQUIRES_TERMINATION)
            .otherwise(NOT_READY_TO_ACCEPT)
    override val status: MemoryUnitStatus by observableStatus

    override fun takeTransition(transition: Transition) {
        if (transition[descriptor.expectedChar] != EPSILON_VALUE)
            track.moveHead(HeadMoveDirection.RIGHT)
    }

    override fun copy() = InputTape(descriptor, Track(track))
}
