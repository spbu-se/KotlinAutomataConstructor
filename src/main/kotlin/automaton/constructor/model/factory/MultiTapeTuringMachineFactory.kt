package automaton.constructor.model.factory

import automaton.constructor.model.automaton.MultiTapeTuringMachine
import automaton.constructor.model.module.tape.MultiTrackTapeDescriptor
import automaton.constructor.utils.Setting
import automaton.constructor.utils.I18N.messages
import javafx.scene.control.Spinner
import tornadofx.*

class MultiTapeTuringMachineFactory : AbstractAutomatonFactory(MultiTapeTuringMachine.NAME) {
    val tapeCountProperty = DEFAULT_TAPE_COUNT.toProperty()
    var tapeCount by tapeCountProperty

    override fun createAutomaton() = MultiTapeTuringMachine(
        tapes = List(tapeCount) { MultiTrackTapeDescriptor(trackCount = 1) }
    )

    override fun createSettings() = listOf(
        Setting(
            displayName = messages.getString("MultiTapeTuringMachineFactory.NumberOfTapes"),
            editor = Spinner<Int>(MIN_TAPE_COUNT, MAX_TAPE_COUNT, DEFAULT_TAPE_COUNT).apply {
                tapeCountProperty.bind(valueProperty())
            }
        )
    )

    companion object {
        const val MIN_TAPE_COUNT = 2
        const val MAX_TAPE_COUNT = 5
        const val DEFAULT_TAPE_COUNT = 2
    }
}
