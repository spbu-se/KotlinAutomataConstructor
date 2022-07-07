package automaton.constructor.model.factory

import automaton.constructor.model.automaton.BaseAutomaton
import automaton.constructor.model.memory.MemoryUnitDescriptor
import automaton.constructor.utils.*

abstract class AbstractAutomatonFactory(override val displayName: String) : AutomatonFactory {
    abstract fun createMemoryDescriptors(): List<MemoryUnitDescriptor>
    abstract fun createSettings(): List<Setting>

    override fun createAutomaton() = BaseAutomaton(displayName, createMemoryDescriptors())
    override fun createEditor() = createSettings().takeIf { it.isNotEmpty() }?.let { SettingListEditor(it) }

    override fun toString() = displayName.capitalize()
}
