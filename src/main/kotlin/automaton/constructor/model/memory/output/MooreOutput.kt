package automaton.constructor.model.memory.output

import automaton.constructor.model.transition.Transition
import automaton.constructor.model.property.DynamicPropertyDescriptor

class MooreOutputDescriptor : AbstractOutputDescriptor() {
    override val transitionSideEffects = emptyList<DynamicPropertyDescriptor<*>>()
    override val stateSideEffects = listOf(outputChar)
    override var displayName = "Moore output"
    override fun getOutputChar(transition: Transition) = transition.target[outputChar]
}
