package automaton.constructor.utils

import javafx.beans.property.Property
import javafx.beans.value.ObservableValue
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.TitledPane
import javafx.scene.layout.GridPane
import javafx.scene.layout.VBox
import tornadofx.*

data class SettingGroup(val observableName: ObservableValue<String>, val settings: List<Setting>) {
    companion object {
        fun fromEditables(observableName: ObservableValue<String>, editables: List<Editable>) =
            SettingGroup(observableName, editables.mapNotNull { it.createSettingOrNull() })
    }
}

fun Editable.createSettingOrNull() = createEditor()?.let { Setting(displayName, it) }

data class Setting(val displayName: String, val editor: Node)

class SettingsEditor : VBox() {
    val settingsProperty = objectProperty<List<SettingGroup>?>(null)
    var settings by settingsProperty

    init {
        settingsProperty.onChange {
            clear()
            settings
                ?.filter { it.settings.isNotEmpty() }
                ?.forEach { add(SettingGroupEditor(it)) }
        }
    }
}

open class SettingGroupEditor(val group: SettingGroup) : TitledPane() {
    val gridpane = SettingListEditor(group.settings).also { add(it) }

    init {
        textProperty().bind(group.observableName)
    }
}

class SettingListEditor(val settings: List<Setting>) : GridPane() {
    init {
        paddingAll = 5.0
        hgap = 25.0
        vgap = 5.0
        settings.forEachIndexed { i, (name, control) ->
            add(Label(name), 0, i)
            add(control, 1, i)
        }
    }
}

fun createUnmodifiableSettingControl(property: Property<String>) = Label().apply { textProperty().bind(property) }
