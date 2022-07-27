package automaton.constructor.model.automaton

import automaton.constructor.model.action.element.createEliminateEpsilonTransitionAction
import automaton.constructor.model.automaton.flavours.AutomatonWithInputTape
import automaton.constructor.model.automaton.flavours.AutomatonWithStacks
import automaton.constructor.model.data.PushdownAutomatonData
import automaton.constructor.model.memory.StackDescriptor
import automaton.constructor.model.memory.tape.InputTapeDescriptor
import automaton.constructor.utils.I18N
import automaton.constructor.utils.I18N.messages

/**
 * Pushdown automaton.
 *
 * It's an automaton with an [input tape][inputTape] and several [stacks] as [memory descriptors][memoryDescriptors].
 */
class PushdownAutomaton(
    override val inputTape: InputTapeDescriptor, override val stacks: List<StackDescriptor>
) : AbstractAutomaton(DISPLAY_NAME, memoryDescriptors = listOf(inputTape) + stacks), AutomatonWithInputTape,
    AutomatonWithStacks {
    init {
        require(stacks.isNotEmpty()) {
            "Illegal `stacks` argument when creating `PushdownAutomaton`"
        }
    }

    override val deterministicDisplayName: String =
        I18N.messages.getString("AutomatonDescriptionProvider.FiniteAutomaton.Deterministic")
    override val nondeterministicDisplayName: String =
        I18N.messages.getString("AutomatonDescriptionProvider.FiniteAutomaton.Nondeterministic")
    override val untitledDisplayName: String =
        I18N.messages.getString("OpenedAutomatonController.UntitledFiniteAutomaton")

    override val transitionActions = super.transitionActions + listOf(
        createEliminateEpsilonTransitionAction(automaton = this)
    )

    override fun getTypeData() =
        PushdownAutomatonData(inputTape = inputTape.getData(), stacks = stacks.map { it.getData() })

    companion object {
        val DISPLAY_NAME: String = messages.getString("PushdownAutomaton")
    }
}
