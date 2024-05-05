package automaton.constructor.view

import automaton.constructor.model.element.AutomatonVertex
import automaton.constructor.model.element.BuildingBlock
import automaton.constructor.model.module.hasProblems
import automaton.constructor.model.module.hasProblemsBinding
import automaton.constructor.utils.I18N
import automaton.constructor.utils.Setting
import automaton.constructor.utils.SettingGroup
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.CheckBox
import javafx.scene.control.TextField
import tornadofx.*

open class AutomatonBasicVertexView(val vertex: AutomatonVertex) : AutomatonElementView(vertex) {
    val colourProperty = SimpleStringProperty("white")
    var colour by colourProperty
    init {
        label {
            textProperty().bind(vertex.nameProperty)
        }
        if (vertex is BuildingBlock) {
            if (vertex.subAutomaton.hasProblems) {
                colour = "red"
            }
            vertex.subAutomaton.hasProblemsBinding.addListener(ChangeListener { _, _, newValue ->
                colour = if (newValue) {
                    "red"
                } else {
                    "white"
                }
            })
        }
    }

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