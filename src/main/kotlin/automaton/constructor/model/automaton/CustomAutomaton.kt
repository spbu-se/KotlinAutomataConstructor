package automaton.constructor.model.automaton

import automaton.constructor.model.data.CustomAutomatonData
import automaton.constructor.model.memory.MemoryUnitDescriptor

/**
 * Custom automaton.
 *
 * It's an automaton with a custom list of [memory descriptors][memoryDescriptors].
 */
class CustomAutomaton(
    memoryDescriptors: List<MemoryUnitDescriptor>
) : AbstractAutomaton(NAME, memoryDescriptors) {
    override fun getTypeData() = CustomAutomatonData(
        memoryUnitDescriptors = memoryDescriptors.map { it.getData() }
    )

    companion object {
        const val NAME = "custom automaton"
    }
}
