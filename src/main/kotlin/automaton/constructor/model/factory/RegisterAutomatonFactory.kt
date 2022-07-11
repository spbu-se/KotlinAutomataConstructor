package automaton.constructor.model.factory

import automaton.constructor.model.automaton.RegisterAutomaton
import automaton.constructor.model.automaton.RegisterAutomaton.Companion.DEFAULT_REGISTER_COUNT
import automaton.constructor.model.automaton.RegisterAutomaton.Companion.MAX_REGISTER_COUNT
import automaton.constructor.model.automaton.RegisterAutomaton.Companion.MIN_REGISTER_COUNT
import automaton.constructor.model.automaton.RegisterAutomaton.Companion.NAME
import automaton.constructor.model.memory.RegisterDescriptor
import automaton.constructor.utils.Setting
import javafx.scene.control.Spinner
import tornadofx.*

class RegisterAutomatonFactory : AbstractAutomatonFactory(NAME) {
    private val registerCountProperty = DEFAULT_REGISTER_COUNT.toProperty()
    var registerCount by registerCountProperty

    override fun createAutomaton() = RegisterAutomaton(
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
