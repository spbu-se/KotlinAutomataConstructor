package automaton.constructor.model.factory

import automaton.constructor.model.Automaton
import automaton.constructor.model.memory.MemoryUnitDescriptor
import automaton.constructor.utils.Setting
import automaton.constructor.utils.SettingListEditor
import java.util.*

abstract class AbstractAutomatonFactory(override val displayName: String) : AutomatonFactory {
    abstract fun createMemoryDescriptors(): List<MemoryUnitDescriptor>
    abstract fun createSettings(): List<Setting>

    override fun createAutomaton() = Automaton(displayName, createMemoryDescriptors())
    override fun createEditor() = createSettings().takeIf { it.isNotEmpty() }?.let { SettingListEditor(it) }

    override fun toString() = displayName.replaceFirstChar { it.titlecase(Locale.getDefault()) }
}
