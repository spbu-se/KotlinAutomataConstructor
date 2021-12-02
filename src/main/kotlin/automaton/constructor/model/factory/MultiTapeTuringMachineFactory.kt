package automaton.constructor.model.factory

import automaton.constructor.model.memory.MemoryUnitDescriptor
import automaton.constructor.model.memory.tape.MultiTrackTapeDescriptor
import automaton.constructor.utils.Setting
import javafx.scene.control.Spinner
import tornadofx.*

class MultiTapeTuringMachineFactory : AbstractAutomatonFactory("multi-tape Turing machine") {
    val tapeCountProperty = 2.toProperty()
    var tapeCount by tapeCountProperty

    override fun createMemoryDescriptors(): List<MemoryUnitDescriptor> =
        List(tapeCount) { MultiTrackTapeDescriptor(1) }

    override fun createSettings() = listOf(
        Setting("Number of tapes", Spinner<Int>(2, 5, tapeCount).apply {
            tapeCountProperty.bind(valueProperty())
        })
    )
}
