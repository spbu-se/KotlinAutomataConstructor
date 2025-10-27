package automaton.constructor.view.elements.transition

import automaton.constructor.model.element.Transition
import automaton.constructor.model.element.touchesRecursiveBox
import automaton.constructor.utils.I18N
import automaton.constructor.utils.SettingGroup
import automaton.constructor.utils.nonNullObjectBinding
import automaton.constructor.utils.translateToCenter
import javafx.beans.binding.Binding
import javafx.beans.property.DoubleProperty
import javafx.scene.paint.Color
import javafx.scene.text.Font
import tornadofx.getValue
import tornadofx.setValue
import tornadofx.text
import tornadofx.toProperty

class TransitionView(
    transition: Transition,
    index: Int
) : BasicTransitionView(transition) {
    val indexProperty = index.toProperty()
    var index by indexProperty
    val xProperty: DoubleProperty
    val yProperty: DoubleProperty

    val colorProperty: Binding<Color> = selectedProperty.nonNullObjectBinding {
        if (selected) Color.BLUE else Color.BLACK
    }

    override fun getSettings(): List<SettingGroup> {
        val dynamicGroups = super.getSettings()
        val expectedLabel = I18N.messages.getString("InputTape.ExpectedChar")
        val processedDynamicGroups =
            if (transition.touchesRecursiveBox()) {
                dynamicGroups.map { group ->
                    group.copy(settings = group.settings.filterNot { it.displayName == expectedLabel })
                }.filter { it.settings.isNotEmpty() }
            } else {
                dynamicGroups
            }
        return processedDynamicGroups
    }

    init {
        val text = text {
            fillProperty().bind(colorProperty)
            font = Font.font(48.0)
            textProperty().bind(transition.propertiesTextBinding)
            translateToCenter()
        }
        xProperty = text.xProperty()
        yProperty = text.yProperty()
    }
}
