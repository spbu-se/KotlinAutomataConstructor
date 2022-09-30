package automaton.constructor.model.automaton

import automaton.constructor.model.data.CustomAutomatonData
import automaton.constructor.model.memory.MemoryUnitDescriptor
import automaton.constructor.utils.I18N

/**
 * Custom automaton.
 *
 * It's an automaton with a custom list of [memory descriptors][memoryDescriptors].
 */
class CustomAutomaton(
    memoryDescriptors: List<MemoryUnitDescriptor>
) : AbstractAutomaton(
    DISPLAY_NAME,
    memoryDescriptors,
    I18N.messages.getString("CustomAutomaton.Deterministic"),
    I18N.messages.getString("CustomAutomaton.Nondeterministic"),
    I18N.messages.getString("CustomAutomaton.Untitled")
) {

    override fun getTypeData() = CustomAutomatonData(
        memoryUnitDescriptors = memoryDescriptors.map { it.getData() }
    )

    override fun createSubAutomaton() = CustomAutomaton(memoryDescriptors)

    companion object {
        val DISPLAY_NAME: String = I18N.messages.getString("CustomAutomaton")
    }
}
