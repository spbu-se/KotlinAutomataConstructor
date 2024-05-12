package automaton.constructor.model.automaton

import automaton.constructor.model.action.transition.EliminateEpsilonTransitionAction
import automaton.constructor.model.action.transition.SimplifyRegexEntirelyTransitionAction
import automaton.constructor.model.action.transition.SimplifyRegexTransitionAction
import automaton.constructor.model.automaton.flavours.AutomatonWithInputTape
import automaton.constructor.model.data.FiniteAutomatonData
import automaton.constructor.model.memory.tape.InputTapeDescriptor
import automaton.constructor.model.transformation.DeterminizeAutomatonAction
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
    I18N.messages.getString("FiniteAutomaton.Untitled")
), AutomatonWithInputTape {
    override fun getTypeData() = FiniteAutomatonData(
        inputTape = inputTape.getData()
    )

    override val transitionActions = super.transitionActions + listOf(
        EliminateEpsilonTransitionAction(automaton = this),
        SimplifyRegexTransitionAction(automaton = this),
        SimplifyRegexEntirelyTransitionAction(automaton = this)
    )

    override val transformationActions = super.transformationActions + listOf(DeterminizeAutomatonAction(this))

    override fun createEmptyAutomatonOfSameType() = FiniteAutomaton(inputTape)

    companion object {
        val DISPLAY_NAME: String = I18N.messages.getString("FiniteAutomaton")
    }
}
