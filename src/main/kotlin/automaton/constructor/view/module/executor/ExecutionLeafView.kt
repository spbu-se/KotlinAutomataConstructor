package automaton.constructor.view.module.executor

import automaton.constructor.model.module.executor.ExecutionState
import automaton.constructor.utils.SettingGroup
import automaton.constructor.utils.SettingGroupEditor
import automaton.constructor.utils.nonNullObjectBinding
import javafx.geometry.Insets
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.layout.CornerRadii
import javafx.scene.paint.Color
import tornadofx.*

fun executionLeafView(executionState: ExecutionState) =
    SettingGroupEditor(
        SettingGroup(
            executionState.state.nameProperty.stringBinding(executionState.statusProperty) {
                "$it [${executionState.status}]"
            },
            executionState.createSettings()
        )
    ).apply {
        gridpane.backgroundProperty().bind(executionState.statusProperty.nonNullObjectBinding {
            Background(
                BackgroundFill(
                    it.color ?: Color.TRANSPARENT,
                    CornerRadii.EMPTY,
                    Insets.EMPTY
                )
            )
        })
    }
