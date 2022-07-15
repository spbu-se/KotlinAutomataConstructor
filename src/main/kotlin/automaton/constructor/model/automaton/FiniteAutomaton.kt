package automaton.constructor.model.automaton

import automaton.constructor.model.memory.tape.InputTapeDescriptor
import automaton.constructor.utils.I18N.labels

/**
 * Finite automaton.
 *
 * It's an automaton with an [input tape][inputTape] as a [memory descriptor][memoryDescriptors].
 */
class FiniteAutomaton(
    val inputTape: InputTapeDescriptor
) : Automaton by BaseAutomaton(NAME, memoryDescriptors = listOf(inputTape)) {
    companion object {
        val NAME: String = labels.getString("FiniteAutomaton.NAME")
    }
}
