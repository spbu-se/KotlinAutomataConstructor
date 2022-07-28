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
) : AbstractAutomaton(
    DISPLAY_NAME,
    memoryDescriptors = listOf(inputTape),
    I18N.messages.getString("FiniteAutomaton.Deterministic"),
    I18N.messages.getString("FiniteAutomaton.Nondeterministic"),
    I18N.messages.getString("OpenedAutomatonController.FiniteAutomaton.Untitled")
), AutomatonWithInputTape {
    override fun getTypeData() = FiniteAutomatonData(
        inputTape = inputTape.getData()
    )

    override val transitionActions = super.transitionActions + listOf(
        createEliminateEpsilonTransitionAction(automaton = this)
    )

    companion object {
        val DISPLAY_NAME: String = I18N.messages.getString("FiniteAutomaton")
    }
}
