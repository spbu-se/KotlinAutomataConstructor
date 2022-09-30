package automaton.constructor.view

import automaton.constructor.model.element.Transition
import automaton.constructor.utils.*
import automaton.constructor.utils.I18N.messages
import javafx.beans.binding.Binding
import javafx.beans.property.DoubleProperty
import javafx.scene.paint.Color
import javafx.scene.text.Font
import tornadofx.*

class TransitionView(
    val transition: Transition,
    index: Int
) : AutomatonElementView(transition) {
    val indexProperty = index.toProperty()
    var index by indexProperty
    val xProperty: DoubleProperty
    val yProperty: DoubleProperty

    val colorProperty: Binding<Color> = selectedProperty.nonNullObjectBinding {
        if (selected) Color.BLUE else Color.BLACK
    }

    override fun getSettings() = listOf(
        SettingGroup(
            messages.getString("TransitionView.Transition").toProperty(), listOf(
                Setting(
                    messages.getString("TransitionView.Source"),
                    createUnmodifiableSettingControl(transition.source.nameProperty)
                ),
                Setting(
                    messages.getString("TransitionView.Target"),
                    createUnmodifiableSettingControl(transition.target.nameProperty)
                )
            )
        )
    ) + super.getSettings()

    init {
        val text = text {
            fillProperty().bind(colorProperty)
            font = Font.font(48.0)
            textProperty().bind(settingsTextBinding)
            translateToCenter()
        }
        xProperty = text.xProperty()
        yProperty = text.yProperty()
    }
}
