package automaton.constructor.model.memory.tape

import automaton.constructor.model.memory.MemoryUnitDescriptor
import automaton.constructor.model.memory.MemoryUnitStatus
import automaton.constructor.model.memory.MemoryUnitStatus.NOT_READY_TO_ACCEPT
import automaton.constructor.model.memory.MemoryUnitStatus.REQUIRES_TERMINATION
import automaton.constructor.model.transition.Transition
import automaton.constructor.model.transition.property.BLANK_CHAR
import automaton.constructor.model.transition.property.EPSILON_VALUE
import automaton.constructor.model.transition.property.TransitionPropertyDescriptor
import automaton.constructor.model.transition.property.createCharOrEpsTransitionPropertyDescriptor
import automaton.constructor.utils.MonospaceEditableString

class InputTapeDescriptor : MonospaceEditableString(), MemoryUnitDescriptor {
    val expectedChar = createCharOrEpsTransitionPropertyDescriptor("Expected char", canBeDeemedEpsilon = true)

    override val filters = listOf(expectedChar)
    override val sideEffects = emptyList<TransitionPropertyDescriptor<*>>()
    override var displayName = "Input tape"

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
