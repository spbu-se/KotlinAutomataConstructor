package automaton.constructor.view.memory

import automaton.constructor.model.Automaton
import automaton.constructor.utils.SettingGroup
import automaton.constructor.utils.SettingGroupEditor
import tornadofx.*

fun inputDataView(automaton: Automaton) = SettingGroupEditor(
    SettingGroup.fromEditables("Input data".toProperty(), automaton.memoryDescriptors)
)
