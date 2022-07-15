package automaton.constructor.model.factory

import automaton.constructor.model.automaton.MultiTrackTuringMachine
import automaton.constructor.model.memory.tape.MultiTrackTapeDescriptor
import automaton.constructor.utils.Setting
import automaton.constructor.utils.I18N.labels
import javafx.scene.control.Spinner
import tornadofx.*

class MultiTrackTuringMachineFactory : AbstractAutomatonFactory(MultiTrackTuringMachine.NAME) {
    val trackCountProperty = DEFAULT_TRACK_COUNT.toProperty()
    var trackCount by trackCountProperty

    override fun createAutomaton() = MultiTrackTuringMachine(
        tracks = MultiTrackTapeDescriptor(trackCount)
    )

    override fun createSettings() = listOf(
        Setting(
            displayName = labels.getString("MultiTrackTuringMachineFactory.DisplayName.NumberOfTracks"),
            editor = Spinner<Int>(MIN_TRACK_COUNT, MAX_TRACK_COUNT, DEFAULT_TRACK_COUNT).apply {
                trackCountProperty.bind(valueProperty())
            }
        )
    )

    companion object {
        const val MIN_TRACK_COUNT = 2
        const val MAX_TRACK_COUNT = 5
        const val DEFAULT_TRACK_COUNT = 2
    }
}
