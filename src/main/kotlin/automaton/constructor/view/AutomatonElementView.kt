package automaton.constructor.view

import automaton.constructor.model.property.AutomatonElement
import automaton.constructor.utils.SettingGroup
import automaton.constructor.utils.createSettings
import tornadofx.*

abstract class AutomatonElementView(private val automatonElement: AutomatonElement) {
    val selectedProperty = false.toProperty()
    var selected by selectedProperty

    open fun getSettings(): List<SettingGroup> =
        automatonElement.propertyGroups.map { (memoryUnit, filters, sideEffects) ->
            SettingGroup(memoryUnit.displayName.toProperty(), (filters + sideEffects).createSettings())
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
