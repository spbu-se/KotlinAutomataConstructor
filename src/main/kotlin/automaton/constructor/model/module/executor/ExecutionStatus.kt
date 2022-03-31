package automaton.constructor.model.module.executor

enum class ExecutionStatus(val displayName: String) {
    RUNNING("Running"),
    ACCEPTED("Accepted"),
    REJECTED("Rejected"),
    FROZEN("Frozen");

    override fun toString() = displayName
}
