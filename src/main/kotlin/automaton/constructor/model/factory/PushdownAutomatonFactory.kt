package automaton.constructor.model.factory

import automaton.constructor.model.automaton.PushdownAutomaton
import automaton.constructor.model.memory.StackDescriptor
import automaton.constructor.model.memory.tape.InputTapeDescriptor
import automaton.constructor.utils.Setting
import javafx.scene.control.Spinner
import tornadofx.*

class PushdownAutomatonFactory : AbstractAutomatonFactory(PushdownAutomaton.NAME) {
    private val stackCountProperty = DEFAULT_STACK_COUNT.toProperty()
    var stackCount by stackCountProperty

    override fun createAutomaton() = PushdownAutomaton(
        inputTape = InputTapeDescriptor(),
        stacks = List(stackCount) { StackDescriptor() }
    )

    override fun createSettings() = listOf(
        Setting(
            displayName = "Number of stacks",
            editor = Spinner<Int>(MIN_STACK_COUNT, MAX_STACK_COUNT, DEFAULT_STACK_COUNT).apply {
                stackCountProperty.bind(valueProperty())
            }
        )
    )

    companion object {
        const val MIN_STACK_COUNT = 1
        const val MAX_STACK_COUNT = 5
        const val DEFAULT_STACK_COUNT = 1
    }
}
