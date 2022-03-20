package automaton.constructor.view.module.executor

import automaton.constructor.model.module.executor.ExecutionStatus
import javafx.scene.paint.Color

val ExecutionStatus.color
    get() = when (this) {
        ExecutionStatus.RUNNING -> null
        ExecutionStatus.ACCEPTED -> Color.LIGHTGREEN
        ExecutionStatus.REJECTED -> Color.DEEPPINK
    }
