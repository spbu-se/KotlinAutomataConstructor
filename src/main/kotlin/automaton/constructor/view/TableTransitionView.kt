package automaton.constructor.view

import automaton.constructor.model.element.Transition
import automaton.constructor.utils.*
import javafx.beans.binding.Binding
import javafx.scene.paint.Color
import tornadofx.toProperty

open class TableTransitionView(val transition: Transition): AutomatonElementView(transition) {
    val colorProperty: Binding<Color> = selectedProperty.nonNullObjectBinding {
        if (selected) Color.AQUA else Color.BLACK
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
