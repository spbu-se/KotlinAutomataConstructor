package automaton.constructor.model.factory

import automaton.constructor.model.automaton.TuringMachineWithRegisters
import automaton.constructor.model.memory.RegisterDescriptor
import automaton.constructor.model.memory.tape.MultiTrackTapeDescriptor
import automaton.constructor.utils.Setting
import javafx.scene.control.Spinner
import tornadofx.*

class TuringMachineWithRegistersFactory : AbstractAutomatonFactory(TuringMachineWithRegisters.NAME) {
    val registerCountProperty = DEFAULT_REGISTER_COUNT.toProperty()
    var registerCount by registerCountProperty

    override fun createAutomaton() = TuringMachineWithRegisters(
        tape = MultiTrackTapeDescriptor(trackCount = 1),
        registers = List(registerCount) { RegisterDescriptor() }
    )

    override fun createSettings() = listOf(
        Setting(
            displayName = "Number of registers",
            editor = Spinner<Int>(MIN_REGISTER_COUNT, MAX_REGISTER_COUNT, DEFAULT_REGISTER_COUNT).apply {
                registerCountProperty.bind(valueProperty())
            }
        )
    )

    companion object {
        const val MIN_REGISTER_COUNT = 1
        const val MAX_REGISTER_COUNT = 5
        const val DEFAULT_REGISTER_COUNT = 1
    }
}
