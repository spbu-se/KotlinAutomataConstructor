package automaton.constructor.model.memory.output

import automaton.constructor.model.transition.Transition
import automaton.constructor.model.property.DynamicPropertyDescriptor

class MealyOutputDescriptor : AbstractOutputDescriptor() {
    override val transitionSideEffects = listOf(outputChar)
    override val stateSideEffects = emptyList<DynamicPropertyDescriptor<*>>()
    override var displayName = "Mealy output"
    override fun getOutputChar(transition: Transition): Char? = transition[outputChar]
}
