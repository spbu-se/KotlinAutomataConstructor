package automaton.constructor.model.memory.tape

enum class HeadMoveDirection(val displayName: String) {
    RIGHT("→"),
    LEFT("←"),
    STAGNATE("•");

    override fun toString() = displayName
}
