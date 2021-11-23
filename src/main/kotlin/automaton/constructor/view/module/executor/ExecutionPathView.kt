package automaton.constructor.view.module.executor

import automaton.constructor.model.module.executor.ExecutionPath
import automaton.constructor.model.module.executor.ExecutionStatus
import automaton.constructor.utils.nonNullObjectBinding
import automaton.constructor.view.memory.MemoryView
import javafx.geometry.Insets
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.layout.CornerRadii
import javafx.scene.paint.Color
import tornadofx.*

fun executionPathView(executionPath: ExecutionPath) =
    MemoryView(
        executionPath.stateProperty.select { it.nameProperty }.stringBinding(executionPath.statusProperty) {
            "$it [${executionPath.status.text}]"
        },
        executionPath.memory
    ).apply {
        gridpane.backgroundProperty().bind(executionPath.statusProperty.nonNullObjectBinding {
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
