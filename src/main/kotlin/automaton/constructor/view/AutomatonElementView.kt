package automaton.constructor.view

import automaton.constructor.model.property.AutomatonElement
import automaton.constructor.utils.SettingGroup
import automaton.constructor.utils.createSettings
import tornadofx.*

open class AutomatonElementView(
    val automatonElement: AutomatonElement
) {
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
                    val filtersString = filters.joinToString(separator = ",")
                    val sideEffectsString = sideEffects.joinToString(separator = ",")
                    if (filtersString.isEmpty() || sideEffectsString.isEmpty()) filtersString + sideEffectsString
                    else "$filtersString/$sideEffectsString"
                }.filter { it.isNotEmpty() }
                .joinToString(separator = ";")
        }
}
