package automaton.constructor.model.automaton

import automaton.constructor.model.action.transition.EliminateEpsilonTransitionAction
import automaton.constructor.model.automaton.flavours.AutomatonWithInputTape
import automaton.constructor.model.data.RecursiveAutomatonData
import automaton.constructor.model.memory.tape.InputTapeDescriptor
import automaton.constructor.utils.I18N

class RecursiveAutomaton(
    override val inputTape: InputTapeDescriptor,
) : AbstractAutomaton(
    DISPLAY_NAME,
    memoryDescriptors = listOf(inputTape),
    I18N.messages.getString("RecursiveAutomaton.Deterministic"),
    I18N.messages.getString("RecursiveAutomaton.Nondeterministic"),
    I18N.messages.getString("RecursiveAutomaton.Untitled")
), AutomatonWithInputTape {

//    private var grammar: ContextFreeGrammar? = null

    override fun getTypeData() = RecursiveAutomatonData(
        inputTape = inputTape.getData()
    )

    override val transitionActions = super.transitionActions + listOf(
        EliminateEpsilonTransitionAction(automaton = this)
//      TODO: make it not an optional action by default when grammar is entered: `ConvertCFGToRecursiveAutomatonAction(automaton = this)`
    )

    override fun createEmptyAutomatonOfSameType() = RecursiveAutomaton(inputTape)

    companion object {
        val DISPLAY_NAME: String = I18N.messages.getString("RecursiveAutomaton")
    }
}