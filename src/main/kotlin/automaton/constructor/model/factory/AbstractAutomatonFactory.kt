package automaton.constructor.model.factory

import automaton.constructor.utils.*

abstract class AbstractAutomatonFactory(final override val displayName: String) : AutomatonFactory {
    open fun createSettings(): List<Setting> = emptyList<Nothing>()

    final override fun createEditor() = createSettings().takeIf { it.isNotEmpty() }?.let { SettingListEditor(it) }

    final override fun toString() = displayName.capitalize()
}
