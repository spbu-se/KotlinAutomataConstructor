package automaton.constructor.model.automaton

import automaton.constructor.model.data.FiniteAutomatonData
import automaton.constructor.model.memory.tape.InputTapeDescriptor

/**
 * Finite automaton.
 *
 * It's an automaton with an [input tape][inputTape] as a [memory descriptor][memoryDescriptors].
 */
class FiniteAutomaton(
    val inputTape: InputTapeDescriptor
) : AbstractAutomaton(NAME, memoryDescriptors = listOf(inputTape)) {
    override fun getTypeData() = FiniteAutomatonData(
        inputTape = inputTape.getData()
    )

    companion object {
        const val NAME = "finite automaton"
    }
}
