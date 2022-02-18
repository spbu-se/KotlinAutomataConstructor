package automaton.constructor.model.memory.output

import automaton.constructor.model.property.DynamicPropertyDescriptor
import automaton.constructor.model.transition.Transition
import automaton.constructor.utils.noPropertiesSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

private const val NAME = "Moore output"

@Serializable(with = MooreOutputDescriptorSerializer::class)
class MooreOutputDescriptor : AbstractOutputDescriptor() {
    override val transitionSideEffects = emptyList<DynamicPropertyDescriptor<*>>()
    override val stateSideEffects = listOf(outputChar)
    override var displayName = NAME
    override fun getOutputChar(transition: Transition) = transition.target[outputChar]
}

object MooreOutputDescriptorSerializer : KSerializer<MooreOutputDescriptor> by noPropertiesSerializer(
    NAME,
    { MooreOutputDescriptor() }
)
