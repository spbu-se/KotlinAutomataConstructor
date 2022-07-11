package automaton.constructor.model.factory

import automaton.constructor.model.automaton.TuringMachineWithRegisters
import automaton.constructor.model.automaton.TuringMachineWithRegisters.Companion.DEFAULT_REGISTER_COUNT
import automaton.constructor.model.automaton.TuringMachineWithRegisters.Companion.MAX_REGISTER_COUNT
import automaton.constructor.model.automaton.TuringMachineWithRegisters.Companion.MIN_REGISTER_COUNT
import automaton.constructor.model.automaton.TuringMachineWithRegisters.Companion.NAME
import automaton.constructor.model.memory.RegisterDescriptor
import automaton.constructor.utils.Setting
import javafx.scene.control.Spinner
import tornadofx.*

class TuringMachineWithRegistersFactory : AbstractAutomatonFactory(NAME) {
    private val registerCountProperty = DEFAULT_REGISTER_COUNT.toProperty()
    var registerCount by registerCountProperty

    override fun createAutomaton() = TuringMachineWithRegisters(
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
}
