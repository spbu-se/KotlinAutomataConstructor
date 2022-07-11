package automaton.constructor.model.factory

import automaton.constructor.model.automaton.MultiTrackTuringMachine
import automaton.constructor.model.automaton.MultiTrackTuringMachine.Companion.DEFAULT_TRACK_COUNT
import automaton.constructor.model.automaton.MultiTrackTuringMachine.Companion.MAX_TRACK_COUNT
import automaton.constructor.model.automaton.MultiTrackTuringMachine.Companion.MIN_TRACK_COUNT
import automaton.constructor.model.automaton.MultiTrackTuringMachine.Companion.NAME
import automaton.constructor.model.memory.tape.MultiTrackTapeDescriptor
import automaton.constructor.utils.Setting
import javafx.scene.control.Spinner
import tornadofx.*

class MultiTrackTuringMachineFactory : AbstractAutomatonFactory(NAME) {
    private val trackCountProperty = DEFAULT_TRACK_COUNT.toProperty()
    var trackCount by trackCountProperty

    override fun createAutomaton() = MultiTrackTuringMachine(
        tracks = MultiTrackTapeDescriptor(trackCount)
    )

    override fun createSettings() = listOf(
        Setting(
            displayName = "Number of tracks",
            editor = Spinner<Int>(MIN_TRACK_COUNT, MAX_TRACK_COUNT, DEFAULT_TRACK_COUNT).apply {
                trackCountProperty.bind(valueProperty())
            }
        )
    )
}
