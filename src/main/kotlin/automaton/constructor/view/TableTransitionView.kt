package automaton.constructor.view

import automaton.constructor.model.element.Transition
import automaton.constructor.utils.I18N
import automaton.constructor.utils.Setting
import automaton.constructor.utils.SettingGroup
import automaton.constructor.utils.createUnmodifiableSettingControl
import tornadofx.toProperty

open class TableTransitionView(val transition: Transition): AutomatonElementView(transition) {
    var textLength = 0

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