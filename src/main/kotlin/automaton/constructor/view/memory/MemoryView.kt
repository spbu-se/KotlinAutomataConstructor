package automaton.constructor.view.memory

import automaton.constructor.model.automaton.Automaton
import automaton.constructor.utils.SettingGroup
import automaton.constructor.utils.SettingGroupEditor
import automaton.constructor.utils.createSettings
import tornadofx.*

fun inputDataView(automaton: Automaton) = SettingGroupEditor(
    SettingGroup("Input data".toProperty(), automaton.memoryDescriptors.createSettings())
)
