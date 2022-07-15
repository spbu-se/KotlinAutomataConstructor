package automaton.constructor.model.memory.output

import automaton.constructor.model.property.DynamicPropertyDescriptor
import automaton.constructor.model.transition.Transition
import automaton.constructor.utils.noPropertiesSerializer
import automaton.constructor.utils.I18N.labels
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

private val NAME: String = labels.getString("MealyOutput.NAME")

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

