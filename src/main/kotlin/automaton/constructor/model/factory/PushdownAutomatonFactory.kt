package automaton.constructor.model.factory

import automaton.constructor.model.automaton.PushdownAutomaton
import automaton.constructor.model.automaton.PushdownAutomaton.Companion.DEFAULT_STACK_COUNT
import automaton.constructor.model.automaton.PushdownAutomaton.Companion.MAX_STACK_COUNT
import automaton.constructor.model.automaton.PushdownAutomaton.Companion.MIN_STACK_COUNT
import automaton.constructor.model.automaton.PushdownAutomaton.Companion.NAME
import automaton.constructor.model.memory.StackDescriptor
import automaton.constructor.utils.Setting
import javafx.scene.control.Spinner
import tornadofx.*

class PushdownAutomatonFactory : AbstractAutomatonFactory(NAME) {
    private val stackCountProperty = DEFAULT_STACK_COUNT.toProperty()
    var stackCount by stackCountProperty

    override fun createAutomaton() = PushdownAutomaton(
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
}
