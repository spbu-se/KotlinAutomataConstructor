package automaton.constructor.model.factory

import automaton.constructor.model.automaton.MultiTapeTuringMachine
import automaton.constructor.model.automaton.MultiTapeTuringMachine.Companion.DEFAULT_TAPE_COUNT
import automaton.constructor.model.automaton.MultiTapeTuringMachine.Companion.MAX_TAPE_COUNT
import automaton.constructor.model.automaton.MultiTapeTuringMachine.Companion.MIN_TAPE_COUNT
import automaton.constructor.model.automaton.MultiTapeTuringMachine.Companion.NAME
import automaton.constructor.model.memory.tape.MultiTrackTapeDescriptor
import automaton.constructor.utils.Setting
import javafx.scene.control.Spinner
import tornadofx.*

class MultiTapeTuringMachineFactory : AbstractAutomatonFactory(NAME) {
    private val tapeCountProperty = DEFAULT_TAPE_COUNT.toProperty()
    var tapeCount by tapeCountProperty

    override fun createAutomaton() = MultiTapeTuringMachine(
        tapes = List(tapeCount) { MultiTrackTapeDescriptor(trackCount = 1) }
    )

    override fun createSettings() = listOf(
        Setting(
            displayName = "Number of tapes",
            editor = Spinner<Int>(MIN_TAPE_COUNT, MAX_TAPE_COUNT, DEFAULT_TAPE_COUNT).apply {
                tapeCountProperty.bind(valueProperty())
            }
        )
    )
}
