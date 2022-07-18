package automaton.constructor.model.automaton

import automaton.constructor.model.data.FiniteAutomatonData
import automaton.constructor.model.memory.tape.InputTapeDescriptor
import automaton.constructor.utils.I18N.messages

/**
 * Finite automaton.
 *
 * It's an automaton with an [input tape][inputTape] as a [memory descriptor][memoryDescriptors].
 */
class FiniteAutomaton(
    val inputTape: InputTapeDescriptor
) : AbstractAutomaton(DISPLAY_NAME, memoryDescriptors = listOf(inputTape)) {
    override fun getTypeData() = FiniteAutomatonData(
        inputTape = inputTape.getData()
    )

    companion object {
        val DISPLAY_NAME: String = messages.getString("FiniteAutomaton")
    }
}
