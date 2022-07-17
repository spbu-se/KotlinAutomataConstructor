package automaton.constructor.view.module.executor

import automaton.constructor.model.module.executor.ExecutionState
import automaton.constructor.model.module.executor.ExecutionStatus
import automaton.constructor.utils.Setting
import automaton.constructor.utils.SettingListEditor
import automaton.constructor.utils.createSettings
import automaton.constructor.utils.nonNullObjectBinding
import automaton.constructor.utils.I18N.messages
import javafx.beans.binding.Bindings.isEmpty
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

val ExecutionState.backgroundBinding
    get() = statusProperty.nonNullObjectBinding {
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
        visibleWhen(isEmpty(children).and(statusProperty.booleanBinding { it == ExecutionStatus.RUNNING || it == ExecutionStatus.FROZEN }))
        managedWhen(visibleProperty())
        selectedProperty().bindBidirectional(isFrozenProperty)
    })

fun ExecutionState.tooltipContent() = SettingListEditor(createSettings())
