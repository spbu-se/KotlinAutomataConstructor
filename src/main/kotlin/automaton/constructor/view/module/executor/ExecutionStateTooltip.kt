package automaton.constructor.view.module.executor

import automaton.constructor.model.element.State
import automaton.constructor.model.module.executor.ExecutionState
import automaton.constructor.utils.HoverableTooltipScope
import automaton.constructor.utils.I18N.messages
import javafx.collections.SetChangeListener
import javafx.geometry.Orientation
import javafx.scene.layout.VBox
import tornadofx.*
import java.text.MessageFormat

private const val MAX_SHOWN_EXECUTION_STATES = 5

fun HoverableTooltipScope.executionStatesTooltip(state: State) =
    if (state.executionStates.isEmpty()) null
    else VBox().apply {
        val listener = SetChangeListener<ExecutionState> { scheduleShow() }
        state.executionStates.addListener(listener)
        onHiding = { state.executionStates.removeListener(listener) }
        state.executionStates
            .sortedBy { it.status }
            .take(MAX_SHOWN_EXECUTION_STATES)
            .forEachIndexed { i, executionState ->
                if (i != 0) separator(Orientation.HORIZONTAL)
                add(executionState.simpleTooltipContent().apply {
                    backgroundProperty().bind(executionState.statusProperty.backgroundBinding)
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
