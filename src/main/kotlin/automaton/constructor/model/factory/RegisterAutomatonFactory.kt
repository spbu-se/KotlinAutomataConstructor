package automaton.constructor.model.factory

import automaton.constructor.model.automaton.RegisterAutomaton
import automaton.constructor.model.memory.RegisterDescriptor
import automaton.constructor.model.memory.tape.InputTapeDescriptor
import automaton.constructor.utils.Setting
import automaton.constructor.utils.I18N.labels
import javafx.scene.control.Spinner
import tornadofx.*

class RegisterAutomatonFactory : AbstractAutomatonFactory(RegisterAutomaton.NAME) {
    val registerCountProperty = DEFAULT_REGISTER_COUNT.toProperty()
    var registerCount by registerCountProperty

    override fun createAutomaton() = RegisterAutomaton(
        inputTape = InputTapeDescriptor(),
        registers = List(registerCount) { RegisterDescriptor() }
    )

    override fun createSettings() = listOf(
        Setting(
            displayName = labels.getString("RegisterAutomatonFactory.DisplayName.NumberOfRegisters"),
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
