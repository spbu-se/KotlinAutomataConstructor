package automaton.constructor.model.automaton

import automaton.constructor.model.memory.StackDescriptor
import automaton.constructor.model.memory.tape.InputTapeDescriptor
import automaton.constructor.utils.I18N.messages

/**
 * Pushdown automaton.
 *
 * It's an automaton with an [input tape][inputTape] and several [stacks] as [memory descriptors][memoryDescriptors].
 */
class PushdownAutomaton(
    val inputTape: InputTapeDescriptor,
    val stacks: List<StackDescriptor>
) : Automaton by BaseAutomaton(NAME, memoryDescriptors = listOf(inputTape) + stacks) {
    init {
        require(stacks.isNotEmpty()) {
            messages.getString("PushDownAutomaton.IllegalStacksArgument")
        }
    }

    companion object {
        val NAME: String = messages.getString("PushdownAutomaton")
    }
}
