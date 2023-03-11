package automaton.constructor.view

import automaton.constructor.model.element.AutomatonElement
import automaton.constructor.utils.SettingGroup
import automaton.constructor.utils.createSettings
import javafx.scene.Group
import tornadofx.*

abstract class AutomatonElementView(val automatonElement: AutomatonElement) : Group() {
    val selectedProperty = false.toProperty()
    var selected by selectedProperty

    open fun getSettings(): List<SettingGroup> =
        automatonElement.propertyGroups.map { (displayName, filters, sideEffects) ->
            SettingGroup(displayName.toProperty(), (filters + sideEffects).createSettings())
        }
}
