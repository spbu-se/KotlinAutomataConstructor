package automaton.constructor.view

import automaton.constructor.model.element.Transition
import automaton.constructor.utils.I18N
import automaton.constructor.utils.Setting
import automaton.constructor.utils.SettingGroup
import automaton.constructor.utils.createUnmodifiableSettingControl
import tornadofx.ChangeListener
import tornadofx.label
import tornadofx.toProperty

class AdjacencyMatrixTransitionView(transition: Transition): TableTransitionView(transition) {
    init {
        label {
            textProperty().bind(transition.propertiesTextBinding)
            textLength = text.length
            textProperty().addListener(ChangeListener { _, oldValue, newValue ->
                textLength = textLength - oldValue.length + newValue.length
            })
        }
    }
}