package automaton.constructor.model.module.executor

import automaton.constructor.utils.I18N

enum class ExecutionStatus(val displayName: String) {
    RUNNING(I18N.messages.getString("ExecutionStatus.RUNNING")),
    ACCEPTED(I18N.messages.getString("ExecutionStatus.ACCEPTED")),
    REJECTED(I18N.messages.getString("ExecutionStatus.REJECTED")),
    FROZEN(I18N.messages.getString("ExecutionStatus.FROZEN"));

    override fun toString() = displayName
}
