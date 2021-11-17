package automaton.constructor.model.memory.tape

import automaton.constructor.model.MemoryUnit.Status.NOT_READY_TO_TERMINATE
import automaton.constructor.model.MemoryUnit.Status.REQUIRES_TERMINATION
import automaton.constructor.model.transition.Transition
import automaton.constructor.model.transition.property.TransitionPropertyDescriptor

class InputTape : AbstractTape {
    override val sideEffectDescriptors: List<TransitionPropertyDescriptor<*>> get() = emptyList()
    override val status get() = if (getChar(headPosition) == BLANK_CHAR) REQUIRES_TERMINATION else NOT_READY_TO_TERMINATE

    constructor() : super("Input tape", canBeDeemedEpsilon = true)
    constructor(inputTape: InputTape) : super(inputTape)

    override fun takeTransition(transition: Transition) {
        if (transition.expectedChar != null) headPosition++
    }

    override fun copy() = InputTape(this)
}
