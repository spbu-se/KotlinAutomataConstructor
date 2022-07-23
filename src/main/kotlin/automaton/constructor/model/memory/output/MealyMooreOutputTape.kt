package automaton.constructor.model.memory.output

import automaton.constructor.model.data.MealyMooreOutputTapeDescriptorData
import automaton.constructor.model.transition.Transition
import automaton.constructor.utils.I18N

class MealyMooreOutputTapeDescriptor : AbstractOutputTapeDescriptor() {
    override val transitionSideEffects = listOf(outputStringDescriptor)
    override val stateSideEffects = listOf(outputStringDescriptor)
    override var displayName: String = I18N.messages.getString("MealyMooreOutputTape")
    override fun getOutput(transition: Transition): String = listOfNotNull(
        transition[outputStringDescriptor],
        transition.target[outputStringDescriptor]
    ).joinToString(separator = "")

    override fun getData() = MealyMooreOutputTapeDescriptorData
}
