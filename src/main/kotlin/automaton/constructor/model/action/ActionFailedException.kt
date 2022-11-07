package automaton.constructor.model.action

class ActionFailedException(override val message: String) : Exception() {
    companion object {
        private const val serialVersionUID: Long = -5243046516530527914L
    }
}
