package automaton.constructor.model.automaton

import automaton.constructor.model.action.element.createEliminateEpsilonTransitionAction
import automaton.constructor.model.automaton.flavours.AutomatonWithInputTape
import automaton.constructor.model.automaton.flavours.AutomatonWithRegisters
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
    override val inputTape: InputTapeDescriptor,
    override val registers: List<RegisterDescriptor>
) : AbstractAutomaton(DISPLAY_NAME, memoryDescriptors = listOf(inputTape) + registers),
    AutomatonWithInputTape, AutomatonWithRegisters {
    init {
        require(registers.isNotEmpty()) {
            messages.getString("RegisterAutomaton.IllegalRegistersArgument")
        }
    }

    override val transitionActions = super.transitionActions + listOf(
        createEliminateEpsilonTransitionAction(automaton = this)
    )

    override fun getTypeData() = RegisterAutomatonData(
        inputTape = inputTape.getData(),
        registers = registers.map { it.getData() }
    )

    companion object {
        val DISPLAY_NAME: String = messages.getString("RegisterAutomaton")
    }
}
