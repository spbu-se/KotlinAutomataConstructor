package automaton.constructor.model.automaton

import automaton.constructor.model.data.RegisterAutomatonData
import automaton.constructor.model.memory.RegisterDescriptor
import automaton.constructor.model.memory.tape.InputTapeDescriptor
import automaton.constructor.utils.I18N.messages

/**
 * Register automaton.
 *
 * It's an automaton with an [input tape][inputTape] and several [registers] as [memory descriptors][memoryDescriptors].
 */
class RegisterAutomaton(
    val inputTape: InputTapeDescriptor,
    val registers: List<RegisterDescriptor>
) : AbstractAutomaton(DISPLAY_NAME, memoryDescriptors = listOf(inputTape) + registers) {
    init {
        require(registers.isNotEmpty()) {
            messages.getString("RegisterAutomaton.IllegalRegistersArgument")
        }
    }

    override fun getTypeData() = RegisterAutomatonData(
        inputTape = inputTape.getData(),
        registers = registers.map { it.getData() }
    )

    companion object {
        val DISPLAY_NAME: String = messages.getString("RegisterAutomaton")
    }
}
