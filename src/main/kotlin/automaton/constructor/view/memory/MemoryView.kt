package automaton.constructor.view.memory

import automaton.constructor.model.automaton.Automaton
import automaton.constructor.utils.I18N.messages
import automaton.constructor.utils.SettingGroup
import automaton.constructor.utils.SettingGroupEditor
import automaton.constructor.utils.createSettings
import javafx.event.EventTarget
import tornadofx.*

fun EventTarget.inputDataView(automaton: Automaton, op: SettingGroupEditor.() -> Unit) = SettingGroupEditor(
    SettingGroup(
        messages.getString("MemoryView.InputData").toProperty(),
        automaton.memoryDescriptors.createSettings()
    )
).also {
    add(it)
    op(it)
}
