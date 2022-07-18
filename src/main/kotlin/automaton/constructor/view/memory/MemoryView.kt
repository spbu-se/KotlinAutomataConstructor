package automaton.constructor.view.memory

import automaton.constructor.model.automaton.Automaton
import automaton.constructor.utils.SettingGroup
import automaton.constructor.utils.SettingGroupEditor
import automaton.constructor.utils.createSettings
import automaton.constructor.utils.I18N.messages
import tornadofx.*

fun inputDataView(automaton: Automaton) = SettingGroupEditor(
    SettingGroup(
        messages.getString("MemoryView.InputData").toProperty(),
        automaton.memoryDescriptors.createSettings()
    )
)
