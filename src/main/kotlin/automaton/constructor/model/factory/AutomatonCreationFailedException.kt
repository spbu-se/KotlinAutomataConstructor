package automaton.constructor.model.factory

class AutomatonCreationFailedException(override val message: String?) : Exception() {
    companion object {
        private const val serialVersionUID: Long = 1439220998173263490L
    }
}
