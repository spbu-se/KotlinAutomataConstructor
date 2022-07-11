package automaton.constructor.model.automaton

import automaton.constructor.model.memory.RegisterDescriptor
import automaton.constructor.model.memory.tape.InputTapeDescriptor

/**
 * Register automaton.
 *
 * It's an automaton with an [input tape][inputTape] and several [registers] [memory descriptors][memoryDescriptors].
 */
class RegisterAutomaton(
    val inputTape: InputTapeDescriptor = InputTapeDescriptor(),
    val registers: List<RegisterDescriptor> = List(DEFAULT_REGISTER_COUNT) { RegisterDescriptor() },
) : Automaton by BaseAutomaton(NAME, memoryDescriptors = listOf(inputTape) + registers) {
    init {
        require(registers.size in MIN_REGISTER_COUNT..MAX_REGISTER_COUNT) {
            "Illegal `registers` argument when creating `RegisterAutomaton`"
        }
    }

    companion object {
        const val NAME = "register automaton"
        const val MIN_REGISTER_COUNT = 1
        const val MAX_REGISTER_COUNT = 5
        const val DEFAULT_REGISTER_COUNT = 1
    }
}
