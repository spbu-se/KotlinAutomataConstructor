package automaton.constructor.view.module.executor

import automaton.constructor.model.module.executor.ExecutionState
import automaton.constructor.utils.hoverableTooltip
import automaton.constructor.utils.I18N.messages
import automaton.constructor.view.StateView
import javafx.collections.SetChangeListener
import javafx.geometry.Orientation
import javafx.scene.layout.VBox
import tornadofx.*
import java.text.MessageFormat

private const val MAX_SHOWN_EXECUTION_STATES = 5

fun StateView.installExecutionStateTooltip() = group.hoverableTooltip {
    if (state.executionStates.isEmpty()) null
    else VBox().apply {
        val listener = SetChangeListener<ExecutionState> { restartTimer() }
        state.executionStates.addListener(listener)
        onHide = { state.executionStates.removeListener(listener) }
        state.executionStates
            .sortedBy { it.status }
            .take(MAX_SHOWN_EXECUTION_STATES)
            .forEachIndexed { i, executionState ->
                if (i != 0) separator(Orientation.HORIZONTAL)
                add(executionState.tooltipContent().apply {
                    backgroundProperty().bind(executionState.backgroundBinding)
                })
            }
        val notShownExecutionStates = state.executionStates.size - MAX_SHOWN_EXECUTION_STATES
        if (notShownExecutionStates > 0) {
            separator(Orientation.HORIZONTAL)
            label(
                MessageFormat.format(
                    messages.getString("ExecutionStateTooltip.NotShownExecutionStates"),
                    notShownExecutionStates
                )
            ) { paddingAll = 5.0 }
        }
    }
}
