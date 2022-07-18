
package automaton.constructor.model.memory.output

import automaton.constructor.model.data.MealyMooreOutputTapeDescriptorData
import automaton.constructor.model.transition.Transition
import automaton.constructor.utils.I18N.messages

class MealyMooreOutputTapeDescriptor : AbstractOutputTapeDescriptor() {
    override val transitionSideEffects = listOf(outputCharDescriptor)
    override val stateSideEffects = listOf(outputCharDescriptor)
    override var displayName: String = messages.getString("MealyMooreOutputTape")
    override fun getOutput(transition: Transition): List<Char?> = listOf(
        transition[outputCharDescriptor],
        transition.target[outputCharDescriptor]
    )

    override fun getData() = MealyMooreOutputTapeDescriptorData
}
