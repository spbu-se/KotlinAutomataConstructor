package automaton.constructor.model.automaton

import automaton.constructor.model.automaton.flavours.AutomatonWithTape
import automaton.constructor.model.data.TuringMachineData
import automaton.constructor.model.memory.tape.MultiTrackTapeDescriptor
import automaton.constructor.utils.I18N
import automaton.constructor.utils.I18N.messages

/**
 * Turing machine.
 *
 * It's an automaton with a [tape] as a [memory descriptor][memoryDescriptors].
 */
class TuringMachine(
    override val tape: MultiTrackTapeDescriptor
) : AbstractAutomaton(DISPLAY_NAME, memoryDescriptors = listOf(tape)),
    AutomatonWithTape {
    init {
        require(tape.trackCount == 1) {
            "Illegal `tape` argument when creating `TuringMachine`"
        }
    }

    override val deterministicDisplayName: String =
        I18N.messages.getString("AutomatonDescriptionProvider.TuringMachine.Deterministic")
    override val nondeterministicDisplayName: String =
        I18N.messages.getString("AutomatonDescriptionProvider.TuringMachine.Nondeterministic")
    override val untitledDisplayName: String =
        I18N.messages.getString("OpenedAutomatonController.UntitledTuringMachine")

    override fun getTypeData() = TuringMachineData(
        tape = tape.getData()
    )

    companion object {
        val DISPLAY_NAME: String = messages.getString("TuringMachine")
    }
}
