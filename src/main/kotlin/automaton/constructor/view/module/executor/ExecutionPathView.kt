package automaton.constructor.view.module.executor

import automaton.constructor.model.module.executor.ExecutionState
import automaton.constructor.model.module.executor.ExecutionStatus
import automaton.constructor.utils.SettingGroup
import automaton.constructor.utils.SettingGroupEditor
import automaton.constructor.utils.nonNullObjectBinding
import javafx.geometry.Insets
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.layout.CornerRadii
import javafx.scene.paint.Color
import tornadofx.*

fun executionStateView(executionState: ExecutionState) =
    SettingGroupEditor(
        SettingGroup.fromEditables(
            executionState.stateProperty.select { it.nameProperty }.stringBinding(executionState.statusProperty) {
                "$it [${executionState.status}]"
            },
            executionState.memory
        )
    ).apply {
        gridpane.backgroundProperty().bind(executionState.statusProperty.nonNullObjectBinding {
            Background(
                BackgroundFill(
                    when (it!!) {
                        ExecutionStatus.RUNNING -> Color.TRANSPARENT
                        ExecutionStatus.ACCEPTED -> Color.LIGHTGREEN
                        ExecutionStatus.REJECTED -> Color.DEEPPINK
                    },
                    CornerRadii.EMPTY,
                    Insets.EMPTY
                )
            )
        })
    }
