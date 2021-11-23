package automaton.constructor.view.memory

import automaton.constructor.model.Automaton
import automaton.constructor.model.MemoryUnit
import automaton.constructor.utils.Setting
import automaton.constructor.utils.SettingGroup
import automaton.constructor.utils.SettingGroupEditor
import javafx.beans.value.ObservableValue
import tornadofx.*

class MemoryView(val observableName: ObservableValue<String>, val memory: List<MemoryUnit>) : SettingGroupEditor(
    SettingGroup(observableName, memory.map { Setting(it.name, it.createEditor()) })
)

fun inputDataView(automaton: Automaton) = MemoryView("Input data".toProperty(), automaton.memory)
