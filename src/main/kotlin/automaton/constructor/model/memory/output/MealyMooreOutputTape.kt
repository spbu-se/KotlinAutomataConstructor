package automaton.constructor.model.memory.output

import automaton.constructor.model.data.MealyMooreOutputTapeDescriptorData
import automaton.constructor.model.transition.Transition

class MealyMooreOutputTapeDescriptor : AbstractOutputTapeDescriptor() {
    override val transitionSideEffects = listOf(outputCharDescriptor)
    override val stateSideEffects = listOf(outputCharDescriptor)
    override var displayName = "Output tape"
    override fun getOutput(transition: Transition): List<Char?> = listOf(
        transition[outputCharDescriptor],
        transition.target[outputCharDescriptor]
    )

    override fun getData() = MealyMooreOutputTapeDescriptorData
}
