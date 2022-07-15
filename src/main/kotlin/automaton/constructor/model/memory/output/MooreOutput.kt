package automaton.constructor.model.memory.output

import automaton.constructor.model.property.DynamicPropertyDescriptor
import automaton.constructor.model.transition.Transition
import automaton.constructor.utils.noPropertiesSerializer
import automaton.constructor.utils.I18N.labels
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

private val NAME: String = labels.getString("MooreOutput.NAME")

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
