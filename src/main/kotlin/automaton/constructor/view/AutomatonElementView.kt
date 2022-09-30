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

    protected var settingsTextBinding =
        stringBinding(
            automatonElement,
            *automatonElement.propertyGroups.flatMap { it.filters + it.sideEffects }.toTypedArray()
        ) {
            propertyGroups.asSequence()
                .map { (_, filters, sideEffects) ->
                    listOf(filters, sideEffects)
                        .map { dynamicProperties -> dynamicProperties.joinToString(separator = ",") { it.displayValue } }
                        .filter { it.isNotEmpty() }
                        .joinToString(separator = "/")
                }
                .filter { it.isNotEmpty() }
                .joinToString(separator = ";")
        }
}
