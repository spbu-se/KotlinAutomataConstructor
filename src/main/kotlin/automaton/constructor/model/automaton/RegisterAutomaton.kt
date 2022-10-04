package automaton.constructor.model.automaton

import automaton.constructor.model.action.transition.createEliminateEpsilonTransitionAction
import automaton.constructor.model.automaton.flavours.AutomatonWithInputTape
import automaton.constructor.model.automaton.flavours.AutomatonWithRegisters
import automaton.constructor.model.data.RegisterAutomatonData
import automaton.constructor.model.memory.RegisterDescriptor
import automaton.constructor.model.memory.tape.InputTapeDescriptor
import automaton.constructor.utils.I18N

/**
 * Register automaton.
 *
 * It's an automaton with an [input tape][inputTape] and several [registers] as [memory descriptors][memoryDescriptors].
 */
class RegisterAutomaton(
    override val inputTape: InputTapeDescriptor,
    override val registers: List<RegisterDescriptor>
) : AbstractAutomaton(
    DISPLAY_NAME,
    memoryDescriptors = listOf(inputTape) + registers,
    I18N.messages.getString("RegisterAutomaton.Deterministic"),
    I18N.messages.getString("RegisterAutomaton.Nondeterministic"),
    I18N.messages.getString("RegisterAutomaton.Untitled")
),
    AutomatonWithInputTape, AutomatonWithRegisters {
    init {
        require(registers.isNotEmpty()) {
            "Illegal `registers` argument when creating `RegisterAutomaton`"
        }
    }

    override val transitionActions = super.transitionActions + listOf(
        createEliminateEpsilonTransitionAction(automaton = this)
    )

    override fun getTypeData() = RegisterAutomatonData(
        inputTape = inputTape.getData(),
        registers = registers.map { it.getData() }
    )

    override fun createSubAutomaton() = RegisterAutomaton(inputTape, registers)

    companion object {
        val DISPLAY_NAME: String = I18N.messages.getString("RegisterAutomaton")
    }
}
