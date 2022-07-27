package automaton.constructor.model.automaton

import automaton.constructor.model.data.CustomAutomatonData
import automaton.constructor.model.memory.MemoryUnitDescriptor
import automaton.constructor.utils.I18N
import automaton.constructor.utils.I18N.messages

/**
 * Custom automaton.
 *
 * It's an automaton with a custom list of [memory descriptors][memoryDescriptors].
 */
class CustomAutomaton(
    memoryDescriptors: List<MemoryUnitDescriptor>
) : AbstractAutomaton(DISPLAY_NAME, memoryDescriptors) {

    override val deterministicDisplayName: String =
        I18N.messages.getString("AutomatonDescriptionProvider.FiniteAutomaton.Deterministic")
    override val nondeterministicDisplayName: String =
        I18N.messages.getString("AutomatonDescriptionProvider.FiniteAutomaton.Nondeterministic")
    override val untitledDisplayName: String =
        I18N.messages.getString("OpenedAutomatonController.UntitledFiniteAutomaton")

    override fun getTypeData() = CustomAutomatonData(
        memoryUnitDescriptors = memoryDescriptors.map { it.getData() }
    )

    companion object {
        val DISPLAY_NAME: String = messages.getString("CustomAutomaton")
    }
}
