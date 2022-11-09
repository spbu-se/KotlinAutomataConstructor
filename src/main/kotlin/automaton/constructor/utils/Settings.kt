package automaton.constructor.utils

import javafx.beans.binding.DoubleBinding
import javafx.beans.property.Property
import javafx.beans.value.ObservableValue
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.TitledPane
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.GridPane
import javafx.scene.layout.VBox
import tornadofx.*

data class SettingGroup(val observableName: ObservableValue<String>, val settings: List<Setting>)

data class Setting(val displayName: String, val editor: Node)

fun Editable.createSettingOrNull() = createEditor()?.let { Setting(displayName, it) }

fun List<Editable>.createSettings() = mapNotNull { it.createSettingOrNull() }

class SettingsEditor : VBox() {
    val settingsProperty = objectProperty<List<SettingGroup>?>(null)
    var settings by settingsProperty
    val editingDisabledProperty = false.toProperty().apply { onChange { updateEditingDisabledForChildren() } }
    var editingDisabled by editingDisabledProperty

    init {
        settingsProperty.onChange {
            clear()
            val groupEditors = (settings ?: return@onChange)
                .filter { it.settings.isNotEmpty() }
                .map { SettingGroupEditor(it) }
                .onEach { add(it) }
            doubleBinding(Unit, *groupEditors.map { it.gridpane.nameColumnWidthBinding }.toTypedArray()) {
                groupEditors.maxOf { it.gridpane.nameColumnWidth }
            }.onChange { width ->
                groupEditors.forEach { it.gridpane.setNameColumnMinWidth(width) }
            }
            updateEditingDisabledForChildren()
        }
    }

    private fun updateEditingDisabledForChildren() {
        children.forEach { (it as SettingGroupEditor).editingDisabled = editingDisabled }
    }
}

open class SettingGroupEditor(group: SettingGroup) : TitledPane() {
    val editingDisabledProperty = false.toProperty()
    var editingDisabled by editingDisabledProperty
    val gridpane = SettingListEditor(group.settings).also {
        add(it)
        it.editingDisabledProperty.bind(editingDisabledProperty)
    }

    init {
        textProperty().bind(group.observableName)
    }
}

class SettingListEditor(val settings: List<Setting>) : GridPane() {
    val editingDisabledProperty = false.toProperty()
    var editingDisabled by editingDisabledProperty

    val nameColumnWidthBinding: DoubleBinding
    val nameColumnWidth: Double get() = nameColumnWidthBinding.value

    init {
        paddingAll = 5.0
        hgap = 25.0
        vgap = 5.0
        val labels = mutableListOf<Label>()
        settings.forEach { (name, control) ->
            row {
                val label = Label(name)
                label.visibleWhen(control.visibleProperty())
                label.managedWhen(control.managedProperty())
                labels.add(label)
                control.disableWhen(editingDisabledProperty)
                add(label)
                add(control)
            }
        }
        nameColumnWidthBinding =
            doubleBinding(Unit, *labels.map { it.widthProperty() }.toTypedArray()) { labels.maxOf { it.width } }
    }

    fun setNameColumnMinWidth(minWidth: Double) {
        if (columnConstraints.isEmpty()) columnConstraints.add(ColumnConstraints())
        columnConstraints[0].minWidth = maxOf(minWidth, columnConstraints[0].minWidth)
    }
}

fun createUnmodifiableSettingControl(property: Property<String>) = Label().apply { textProperty().bind(property) }
