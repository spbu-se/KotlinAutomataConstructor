package automaton.constructor.view.module.executor

import automaton.constructor.model.module.executor.ExecutionState
import automaton.constructor.utils.SettingGroup
import automaton.constructor.utils.SettingGroupEditor
import tornadofx.*

fun executionLeafView(executionState: ExecutionState) =
    SettingGroupEditor(
        SettingGroup(
            executionState.state.nameProperty.stringBinding(executionState.statusProperty) {
                "$it ${executionState.status}"
            }, executionState.createSettings()
        )
    ).apply {
        gridpane.backgroundProperty().bind(executionState.backgroundBinding)
    }
