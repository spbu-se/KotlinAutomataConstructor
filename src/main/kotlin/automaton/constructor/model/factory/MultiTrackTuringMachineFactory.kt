package automaton.constructor.model.factory

import automaton.constructor.model.memory.MemoryUnitDescriptor
import automaton.constructor.model.memory.tape.MultiTrackTapeDescriptor
import automaton.constructor.utils.Setting
import javafx.scene.control.Spinner
import tornadofx.*

class MultiTrackTuringMachineFactory : AbstractAutomatonFactory("multi-track Turing machine") {
    val trackCountProperty = 2.toProperty()
    var trackCount by trackCountProperty

    override fun createMemoryDescriptors(): List<MemoryUnitDescriptor> =
        listOf(MultiTrackTapeDescriptor(trackCount))

    override fun createSettings() = listOf(
        Setting("Number of tracks", Spinner<Int>(2, 5, trackCount).apply {
            trackCountProperty.bind(valueProperty())
        })
    )
}
