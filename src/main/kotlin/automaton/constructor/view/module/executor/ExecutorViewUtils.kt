package automaton.constructor.view.module.executor

import automaton.constructor.model.module.executor.ExecutionState
import automaton.constructor.model.module.executor.ExecutionStatus
import automaton.constructor.utils.Setting
import automaton.constructor.utils.createSettings
import javafx.beans.binding.Bindings.isEmpty
import javafx.scene.control.CheckBox
import javafx.scene.paint.Color
import tornadofx.*

val ExecutionStatus.color
    get() = when (this) {
        ExecutionStatus.RUNNING -> null
        ExecutionStatus.ACCEPTED -> Color.LIGHTGREEN
        ExecutionStatus.REJECTED -> Color.DEEPPINK
        ExecutionStatus.FROZEN -> Color.DEEPSKYBLUE
    }

fun ExecutionState.createSettings() = memory.createSettings() + Setting("Frozen", CheckBox().apply {
    visibleWhen(isEmpty(children).and(statusProperty.booleanBinding { it == ExecutionStatus.RUNNING || it == ExecutionStatus.FROZEN }))
    managedWhen(visibleProperty())
    selectedProperty().bindBidirectional(isFrozenProperty)
})
