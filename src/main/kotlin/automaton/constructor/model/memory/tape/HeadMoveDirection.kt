package automaton.constructor.model.memory.tape

enum class HeadMoveDirection(val displayName: String, val shift: Int) {
    RIGHT("R", 1),
    LEFT("L", -1),
    STAGNATE("S", 0);

    override fun toString() = displayName
}
