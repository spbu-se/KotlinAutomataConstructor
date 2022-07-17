package automaton.constructor.model.automaton

import automaton.constructor.model.data.CustomAutomatonData
import automaton.constructor.model.memory.MemoryUnitDescriptor

/**
 * Custom automaton.
 *
 * It's an automaton with a custom list of [memory descriptors][memoryDescriptors].
 */
class CustomAutomaton(
    override val memoryDescriptors: List<MemoryUnitDescriptor>
) : Automaton by BaseAutomaton(NAME, memoryDescriptors) {
    override fun getTypeDataOrNull() = CustomAutomatonData(
        memoryUnitDescriptors = memoryDescriptors.map { it.getData() }
    )

    companion object {
        const val NAME = "custom automaton"
    }
}
