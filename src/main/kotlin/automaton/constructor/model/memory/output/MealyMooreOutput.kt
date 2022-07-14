package automaton.constructor.model.memory.output

import automaton.constructor.model.transition.Transition
import automaton.constructor.utils.noPropertiesSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

private const val NAME = "Mealy/Moore output"

@Serializable(with = MealyMooreOutputDescriptorSerializer::class)
class MealyMooreOutputDescriptor : AbstractOutputDescriptor() {
    override val transitionSideEffects = listOf(outputCharDescriptor)
    override val stateSideEffects = listOf(outputCharDescriptor)
    override var displayName = NAME
    override fun getOutput(transition: Transition): List<Char?> = listOf(
        transition[outputCharDescriptor],
        transition.target[outputCharDescriptor]
    )
}

object MealyMooreOutputDescriptorSerializer : KSerializer<MealyMooreOutputDescriptor> by noPropertiesSerializer(
    NAME,
    { MealyMooreOutputDescriptor() }
)
