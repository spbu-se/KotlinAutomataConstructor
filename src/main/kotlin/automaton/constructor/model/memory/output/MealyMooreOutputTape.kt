package automaton.constructor.model.memory.output

import automaton.constructor.model.transition.Transition
import automaton.constructor.utils.noPropertiesSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

private const val NAME = "Mealy/Moore output tape"

@Serializable(with = MealyMooreOutputTapeDescriptorSerializer::class)
class MealyMooreOutputTapeDescriptor : AbstractOutputTapeDescriptor() {
    override val transitionSideEffects = listOf(outputCharDescriptor)
    override val stateSideEffects = listOf(outputCharDescriptor)
    override var displayName = NAME
    override fun getOutput(transition: Transition): List<Char?> = listOf(
        transition[outputCharDescriptor],
        transition.target[outputCharDescriptor]
    )
}

object MealyMooreOutputTapeDescriptorSerializer : KSerializer<MealyMooreOutputTapeDescriptor> by noPropertiesSerializer(
    NAME,
    { MealyMooreOutputTapeDescriptor() }
)
