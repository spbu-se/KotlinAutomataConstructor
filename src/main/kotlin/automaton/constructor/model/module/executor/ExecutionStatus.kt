package automaton.constructor.model.module.executor

enum class ExecutionStatus(val displayName: String) {
    RUNNING("Running"),
    ACCEPTED("Accepted"),
    REJECTED("Rejected");

    override fun toString() = displayName
}
