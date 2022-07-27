package automaton.constructor.model.automaton

import automaton.constructor.model.action.element.createEliminateEpsilonTransitionAction
import automaton.constructor.model.automaton.flavours.AutomatonWithInputTape
import automaton.constructor.model.data.FiniteAutomatonData
import automaton.constructor.model.memory.tape.InputTapeDescriptor
import automaton.constructor.utils.I18N

/**
 * Finite automaton.
 *
 * It's an automaton with an [input tape][inputTape] as a [memory descriptor][memoryDescriptors].
 */
class FiniteAutomaton(
    override val inputTape: InputTapeDescriptor
) : AbstractAutomaton(DISPLAY_NAME, memoryDescriptors = listOf(inputTape)),
    AutomatonWithInputTape {
    override fun getTypeData() = FiniteAutomatonData(
        inputTape = inputTape.getData()
    )

    override val deterministicDisplayName: String =
        I18N.messages.getString("AutomatonDescriptionProvider.FiniteAutomaton.Deterministic")
    override val nondeterministicDisplayName: String =
        I18N.messages.getString("AutomatonDescriptionProvider.FiniteAutomaton.Nondeterministic")
    override val untitledDisplayName: String =
        I18N.messages.getString("OpenedAutomatonController.UntitledFiniteAutomaton")

    override val transitionActions = super.transitionActions + listOf(
        createEliminateEpsilonTransitionAction(automaton = this)
    )

    companion object {
        val DISPLAY_NAME: String = I18N.messages.getString("FiniteAutomaton")
    }
}
