package automaton.constructor.model.memory.tape

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
import automaton.constructor.utils.noPropertiesSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

private const val NAME = "Input tape"

@Serializable(with = InputTapeDescriptorSerializer::class)
class InputTapeDescriptor : MonospaceEditableString(), MemoryUnitDescriptor {
    val expectedChar = DynamicPropertyDescriptors.charOrEps("Expected char", canBeDeemedEpsilon = true)

    override val transitionFilters = listOf(expectedChar)
    override val transitionSideEffects = emptyList<DynamicPropertyDescriptor<*>>()
    override var displayName = NAME
    override val isAlwaysReadyToTerminate get() = false

    override fun createMemoryUnit() = InputTape(this, Track(value))
}

class InputTape(
    override val descriptor: InputTapeDescriptor,
    val track: Track
) : AbstractTape(listOf(track)) {
    override val status: MemoryUnitStatus
        get() = if (track.currentChar == BLANK_CHAR) REQUIRES_TERMINATION else NOT_READY_TO_ACCEPT

    override fun takeTransition(transition: Transition) {
        if (transition[descriptor.expectedChar] != EPSILON_VALUE)
            track.shiftHead(1)
    }

    override fun copy() = InputTape(descriptor, Track(track))
}

object InputTapeDescriptorSerializer : KSerializer<InputTapeDescriptor> by noPropertiesSerializer(
    NAME,
    { InputTapeDescriptor() }
)
