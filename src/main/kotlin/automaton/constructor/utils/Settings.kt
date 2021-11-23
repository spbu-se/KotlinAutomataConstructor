package automaton.constructor.utils

import javafx.beans.property.Property
import javafx.beans.value.ObservableValue
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.TitledPane
import javafx.scene.layout.VBox
import tornadofx.*

data class SettingGroup(val observableName: ObservableValue<String>, val settings: List<Setting>)

data class Setting(val name: String, val editor: Node)

interface SettingsHolder {
    fun getSettings(): List<SettingGroup>
}

class SettingsEditor : VBox() {
    val settingsHolderProperty = objectProperty<SettingsHolder?>(null)
    var settingsHolder: SettingsHolder? by settingsHolderProperty

    init {
        settingsHolderProperty.onChange {
            clear()
            settingsHolder?.getSettings()?.forEach {
                add(SettingGroupEditor(it))
            }
        }
    }
}

open class SettingGroupEditor(val group: SettingGroup) : TitledPane() {
    val gridpane = gridpane {
        hgap = 25.0
        vgap = 5.0
        group.settings.forEachIndexed { i, (name, control) ->
            add(Label(name), 0, i)
            add(control, 1, i)
        }
    }

    init {
        textProperty().bind(group.observableName)
    }
}

fun createUnmodifiableSettingControl(property: Property<String>) = Label().apply { textProperty().bind(property) }
