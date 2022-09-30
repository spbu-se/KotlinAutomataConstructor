package automaton.constructor.view.module.executor

import automaton.constructor.model.module.executor.ExecutionState
import automaton.constructor.utils.SettingGroup
import automaton.constructor.utils.SettingGroupEditor
import tornadofx.*

private const val MAX_TITLE_LENGTH = 50

fun executionLeafView(executionState: ExecutionState) =
    SettingGroupEditor(
        SettingGroup(
            executionState.observableText.stringBinding(executionState.statusProperty) {
                val title = "$it [${executionState.status}]"
                if (title.length <= MAX_TITLE_LENGTH) title
                else "..." + title.takeLast(MAX_TITLE_LENGTH - "...".length)
            }, executionState.createSettings()
        )
    ).apply {
        gridpane.backgroundProperty().bind(
            executionState.observableDeepStatus.backgroundBinding
        )
    }
