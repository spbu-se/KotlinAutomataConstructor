package automaton.constructor.model.automaton

import automaton.constructor.model.memory.StackDescriptor
import automaton.constructor.model.memory.tape.InputTapeDescriptor

/**
 * Pushdown automaton.
 *
 * It's an automaton with an [input tape][inputTape] and several [stacks] [memory descriptors][memoryDescriptors].
 */
class PushdownAutomaton(
    val inputTape: InputTapeDescriptor = InputTapeDescriptor(),
    val stacks: List<StackDescriptor> = List(DEFAULT_STACK_COUNT) { StackDescriptor() },
) : Automaton by BaseAutomaton(NAME, memoryDescriptors = listOf(inputTape) + stacks) {
    init {
        require(stacks.size in MIN_STACK_COUNT..MAX_STACK_COUNT) {
            "Illegal `stacks` argument when creating `PushdownAutomaton`"
        }
    }

    companion object {
        const val NAME = "pushdown automaton"
        const val MIN_STACK_COUNT = 1
        const val MAX_STACK_COUNT = 5
        const val DEFAULT_STACK_COUNT = 1
    }
}
