package automaton.constructor.model.automaton

import automaton.constructor.model.data.CustomAutomatonData
import automaton.constructor.model.memory.MemoryUnitDescriptor
import automaton.constructor.utils.I18N.messages

/**
 * Custom automaton.
 *
 * It's an automaton with a custom list of [memory descriptors][memoryDescriptors].
 */
class CustomAutomaton(
    memoryDescriptors: List<MemoryUnitDescriptor>
) : AbstractAutomaton(DISPLAY_NAME, memoryDescriptors) {
    override fun getTypeData() = CustomAutomatonData(
        memoryUnitDescriptors = memoryDescriptors.map { it.getData() }
    )

    companion object {
        val DISPLAY_NAME: String = messages.getString("CustomAutomaton")
    }
}
