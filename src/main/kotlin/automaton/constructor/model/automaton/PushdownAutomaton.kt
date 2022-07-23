package automaton.constructor.model.automaton

import automaton.constructor.model.action.EliminateEpsilonTransitionAction
import automaton.constructor.model.data.PushdownAutomatonData
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
) : AbstractAutomaton(DISPLAY_NAME, memoryDescriptors = listOf(inputTape) + stacks) {
    init {
        require(stacks.isNotEmpty()) {
            messages.getString("PushDownAutomaton.IllegalStacksArgument")
        }
    }

    override val transitionActions = super.transitionActions + listOf(
        EliminateEpsilonTransitionAction(automaton = this)
    )

    override fun getTypeData() = PushdownAutomatonData(
        inputTape = inputTape.getData(),
        stacks = stacks.map { it.getData() }
    )

    companion object {
        val DISPLAY_NAME: String = messages.getString("PushdownAutomaton")
    }
}
