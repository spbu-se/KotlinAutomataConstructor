package automaton.constructor.view

import automaton.constructor.model.element.Transition
import automaton.constructor.utils.I18N
import automaton.constructor.utils.Setting
import automaton.constructor.utils.SettingGroup
import automaton.constructor.utils.createUnmodifiableSettingControl
import tornadofx.ChangeListener
import tornadofx.label
import tornadofx.toProperty

class AdjacencyMatrixTransitionView(val transition: Transition): AutomatonElementView(transition) {
    var textLength = 0

    init {
        label {
            textProperty().bind(transition.propertiesTextBinding)
            textLength = text.length
            textProperty().addListener(ChangeListener { _, oldValue, newValue ->
                textLength = textLength - oldValue.length + newValue.length
            })
        }
    }

    override fun getSettings() = listOf(
        SettingGroup(
            I18N.messages.getString("TransitionView.Transition").toProperty(), listOf(
                Setting(
                    I18N.messages.getString("TransitionView.Source"),
                    createUnmodifiableSettingControl(transition.source.nameProperty)
                ),
                Setting(
                    I18N.messages.getString("TransitionView.Target"),
                    createUnmodifiableSettingControl(transition.target.nameProperty)
                )
            )
        )
    ) + super.getSettings()
}