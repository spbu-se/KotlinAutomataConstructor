package automaton.constructor.view

import automaton.constructor.model.element.AutomatonVertex
import automaton.constructor.model.element.BuildingBlock
import automaton.constructor.model.module.hasProblems
import automaton.constructor.model.module.hasProblemsBinding
import automaton.constructor.utils.I18N
import automaton.constructor.utils.Setting
import automaton.constructor.utils.SettingGroup
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.CheckBox
import javafx.scene.control.TextField
import javafx.scene.paint.Color
import tornadofx.*

open class AutomatonBasicVertexView(val vertex: AutomatonVertex) : AutomatonElementView(vertex) {
    val colourProperty = SimpleStringProperty("none")
    private var colour by colourProperty
    init {
        hbox {
            label {
                textProperty().bind(vertex.nameProperty)
                textFill = Color.BLACK
            }
            val startFinalCount = SimpleIntegerProperty(0)
            if (vertex.isInitial) {
                startFinalCount.set(1)
            }
            if (vertex.isFinal) {
                startFinalCount.set(startFinalCount.value + 1)
            }
            val startFinalLabel = label {
                if (startFinalCount.value == 1) {
                    if (vertex.isInitial) {
                        text = " (start)"
                    } else {
                        text = " (final)"
                    }
                    text = if (vertex.isInitial) {
                        " (start)"
                    } else {
                        " (final)"
                    }
                }
                if (startFinalCount.value == 2) {
                    text = " (start, final)"
                }
                textFill = Color.BLACK
                isVisible = vertex.isInitial || vertex.isFinal
            }
            vertex.isInitialProperty.addListener { _, _, newValue ->
                if (newValue) {
                    startFinalCount.set(startFinalCount.value + 1)
                } else {
                    startFinalCount.set(startFinalCount.value - 1)
                }
            }
            vertex.isFinalProperty.addListener { _, _, newValue ->
                if (newValue) {
                    startFinalCount.set(startFinalCount.value + 1)
                } else {
                    startFinalCount.set(startFinalCount.value - 1)
                }
            }
            startFinalCount.addListener { _, _, newValue ->
                when (newValue) {
                    0 -> startFinalLabel.isVisible = false
                    1 -> {
                        startFinalLabel.text = if (vertex.isInitial) {
                            " (start)"
                        } else {
                            " (final)"
                        }
                        startFinalLabel.isVisible = true
                    }
                    else -> {
                        startFinalLabel.text = " (start, final)"
                        startFinalLabel.isVisible = true
                    }
                }
            }
        }
        if (vertex is BuildingBlock) {
            if (vertex.subAutomaton.hasProblems) {
                colour = "red"
            }
            vertex.subAutomaton.hasProblemsBinding.addListener(ChangeListener { _, _, newValue ->
                colour = if (newValue) {
                    "red"
                } else {
                    "none"
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
