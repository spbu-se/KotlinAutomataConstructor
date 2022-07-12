package automaton.constructor.model.automaton

import automaton.constructor.model.memory.tape.InputTapeDescriptor

/**
 * Finite automaton.
 *
 * It's an automaton with an [input tape][inputTape] as a [memory descriptor][memoryDescriptors].
 */
class FiniteAutomaton(
    val inputTape: InputTapeDescriptor
) : Automaton by BaseAutomaton(NAME, memoryDescriptors = listOf(inputTape)) {
    companion object {
        const val NAME = "finite automaton"
    }
}