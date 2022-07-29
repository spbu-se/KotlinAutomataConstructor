package automaton.constructor.model.memory.tape

enum class HeadMoveDirection(val displayName: String, val shift: Int) {
    RIGHT("→", 1),
    LEFT("←", -1),
    STAGNATE("•", 0);

    override fun toString() = displayName
}
