package automaton.constructor.view

import automaton.constructor.model.transition.Transition
import automaton.constructor.utils.*
import javafx.beans.binding.Binding
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.Text
import tornadofx.*

class TransitionView(
    val transition: Transition,
    index: Int
) : Text(), SettingsHolder {
    val selectedProperty = false.toProperty()
    var selected by selectedProperty

    val indexProperty = index.toProperty()
    var index by indexProperty

    val colorProperty: Binding<Color> = selectedProperty.nonNullObjectBinding {
        if (selected) Color.BLUE else Color.BLACK
    }

    override fun getSettings() = listOf(
        SettingGroup(
            "Transition".toProperty(), listOf(
                Setting("Source", createUnmodifiableSettingControl(transition.source.nameProperty)),
                Setting("Target", createUnmodifiableSettingControl(transition.target.nameProperty))
            )
        )
    ) + transition.propertyGroups.map { (memoryUnit, filters, sideEffects) ->
        SettingGroup(
            memoryUnit.name.toProperty(),
            (filters + sideEffects).map { Setting(it.name, it.createSettingControl()) })
    }

    init {
        fillProperty().bind(colorProperty)
        font = Font.font(48.0)
        textProperty().bind(stringBinding(transition, *transition.allProperties.toTypedArray()) {
            propertyGroups.joinToString(separator = ";") { (_, filters, sideEffects) ->
                val filtersString = filters.joinToString(separator = ",") { it.stringValue }
                val sideEffectsString = sideEffects.joinToString(separator = ",") { it.stringValue }
                if (filtersString.isEmpty() || sideEffectsString.isEmpty()) filtersString + sideEffectsString
                else "$filtersString/$sideEffectsString"
            }
        })
    }
}
