package automaton.constructor.view

import automaton.constructor.model.element.AutomatonVertex
import automaton.constructor.utils.I18N
import automaton.constructor.utils.Setting
import automaton.constructor.utils.SettingGroup
import javafx.scene.control.CheckBox
import javafx.scene.control.TextField
import tornadofx.*

open class AutomatonBasicVertexView(val vertex: AutomatonVertex) : AutomatonElementView(vertex) {
    override fun getSettings() = listOf(
        SettingGroup(
            I18N.messages.getString("StateView.State").toProperty(), listOf(
                Setting(I18N.messages.getString("StateView.Name"),
                    TextField().apply { textProperty().bindBidirectional(vertex.nameProperty) }),
                Setting(
                    I18N.messages.getString("StateView.Initial"),
                    CheckBox().apply { selectedProperty().bindBidirectional(vertex.isInitialProperty) })
            ) + if (vertex.alwaysEffectivelyFinal) emptyList() else listOf(
                Setting(
                    I18N.messages.getString("StateView.Final"),
                    CheckBox().apply { selectedProperty().bindBidirectional(vertex.isFinalProperty) })
            )
        )
    ) + super.getSettings()
}
