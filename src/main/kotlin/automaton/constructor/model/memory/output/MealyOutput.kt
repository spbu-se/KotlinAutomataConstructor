package automaton.constructor.model.memory.output

import automaton.constructor.model.property.DynamicPropertyDescriptor
import automaton.constructor.model.transition.Transition
import automaton.constructor.utils.noPropertiesSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

private const val NAME = "Mealy output"

@Serializable(with = MealyOutputDescriptorSerializer::class)
class MealyOutputDescriptor : AbstractOutputDescriptor() {
    override val transitionSideEffects = listOf(outputChar)
    override val stateSideEffects = emptyList<DynamicPropertyDescriptor<*>>()
    override var displayName = NAME
    override fun getOutputChar(transition: Transition): Char? = transition[outputChar]
}

object MealyOutputDescriptorSerializer : KSerializer<MealyOutputDescriptor> by noPropertiesSerializer(
    NAME,
    { MealyOutputDescriptor() }
)

