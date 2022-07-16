package automaton.constructor.model.memory.output

import automaton.constructor.model.transition.Transition
import automaton.constructor.utils.noPropertiesSerializer
import automaton.constructor.utils.I18N.labels
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

private val NAME: String = labels.getString("MealyMooreOutputTape.NAME")

@Serializable(with = MealyMooreOutputTapeDescriptorSerializer::class)
class MealyMooreOutputTapeDescriptor : AbstractOutputTapeDescriptor() {
    override val transitionSideEffects = listOf(outputCharDescriptor)
    override val stateSideEffects = listOf(outputCharDescriptor)
    override var displayName: String = labels.getString("MealyMooreOutputTape.DisplayName")
    override fun getOutput(transition: Transition): List<Char?> = listOf(
        transition[outputCharDescriptor],
        transition.target[outputCharDescriptor]
    )
}

object MealyMooreOutputTapeDescriptorSerializer : KSerializer<MealyMooreOutputTapeDescriptor> by noPropertiesSerializer(
    NAME,
    { MealyMooreOutputTapeDescriptor() }
)
