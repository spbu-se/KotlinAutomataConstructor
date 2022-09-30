package automaton.constructor.view.module.executor

import automaton.constructor.model.module.executor.ExecutionState
import automaton.constructor.model.module.executor.ExecutionStatus
import automaton.constructor.utils.I18N.messages
import automaton.constructor.utils.Setting
import automaton.constructor.utils.SettingListEditor
import automaton.constructor.utils.createSettings
import automaton.constructor.utils.nonNullObjectBinding
import javafx.beans.value.ObservableValue
import javafx.geometry.Insets
import javafx.scene.control.CheckBox
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.layout.CornerRadii
import javafx.scene.paint.Color
import tornadofx.*

private val REJECTING_COLOR: Color = Color.rgb(255, 100, 100)

val ExecutionStatus.color
    get() = when (this) {
        ExecutionStatus.RUNNING -> null
        ExecutionStatus.ACCEPTED -> Color.LIGHTGREEN
        ExecutionStatus.REJECTED -> REJECTING_COLOR
        ExecutionStatus.FROZEN -> Color.DEEPSKYBLUE
    }

val ObservableValue<ExecutionStatus>.backgroundBinding
    get() = nonNullObjectBinding {
        Background(
            BackgroundFill(
                it.color ?: Color.TRANSPARENT,
                CornerRadii.EMPTY,
                Insets.EMPTY
            )
        )
    }

fun ExecutionState.createSettings() = memory.createSettings() + Setting(messages.getString("ExecutorViewUtils.Frozen"),
    CheckBox().apply {
        visibleWhen(canHaveMoreChildrenProperty)
        managedWhen(visibleProperty())
        selectedProperty().bindBidirectional(isFrozenProperty)
    })

fun ExecutionState.simpleTooltipContent() = SettingListEditor(createSettings())

