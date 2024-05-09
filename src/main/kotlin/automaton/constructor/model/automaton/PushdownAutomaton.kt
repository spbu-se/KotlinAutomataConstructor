package automaton.constructor.model.automaton

import automaton.constructor.model.action.transition.EliminateEpsilonTransitionAction
import automaton.constructor.model.automaton.flavours.AutomatonWithInputTape
import automaton.constructor.model.automaton.flavours.AutomatonWithStacks
import automaton.constructor.model.data.PushdownAutomatonData
import automaton.constructor.model.memory.MemoryUnitDescriptor
import automaton.constructor.model.memory.StackDescriptor
import automaton.constructor.model.memory.tape.InputTapeDescriptor
import automaton.constructor.utils.I18N

/**
 * Pushdown automaton.
 *
 * It's an automaton with an [input tape][inputTape] and several [stacks] as [memory descriptors][memoryDescriptors].
 */
class PushdownAutomaton(
    override val inputTape: InputTapeDescriptor, override val stacks: List<StackDescriptor>
) : AbstractAutomaton(
    DISPLAY_NAME,
    memoryDescriptors = listOf(inputTape) + stacks,
    I18N.messages.getString("PushdownAutomaton.Deterministic"),
    I18N.messages.getString("PushdownAutomaton.Nondeterministic"),
    I18N.messages.getString("PushdownAutomaton.Untitled")
), AutomatonWithInputTape,
    AutomatonWithStacks {
    init {
        require(stacks.isNotEmpty()) {
            "Illegal `stacks` argument when creating `PushdownAutomaton`"
        }
    }

    override val transitionActions = super.transitionActions + listOf(
        EliminateEpsilonTransitionAction(automaton = this)
    )

    override fun getTypeData() =
        PushdownAutomatonData(inputTape = inputTape.getData(), stacks = stacks.map { it.getData() })

    override fun createEmptyAutomatonOfSameType() = PushdownAutomaton(inputTape, stacks)

    companion object {
        val DISPLAY_NAME: String = I18N.messages.getString("PushdownAutomaton")
    }
}
