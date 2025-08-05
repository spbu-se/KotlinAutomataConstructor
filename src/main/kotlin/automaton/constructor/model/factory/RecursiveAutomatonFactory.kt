package automaton.constructor.model.factory

import automaton.constructor.controller.grammar.RecursiveAutomatonGrammarInputController
import automaton.constructor.model.automaton.RecursiveAutomaton
import automaton.constructor.model.element.ContextFreeGrammar
import automaton.constructor.model.memory.tape.InputTapeDescriptor
import automaton.constructor.utils.I18N
import automaton.constructor.utils.Setting
import javafx.scene.control.Button
import tornadofx.*

class RecursiveAutomatonFactory : AbstractAutomatonFactory(RecursiveAutomaton.DISPLAY_NAME) {
    val grammarProperty = ContextFreeGrammar().toProperty()
    var grammar: ContextFreeGrammar? by grammarProperty
    val controller = RecursiveAutomatonGrammarInputController(this)

    override fun createAutomaton() = RecursiveAutomaton(inputTape = InputTapeDescriptor()).apply {
        grammar?.let {
            controller.buildRecursiveAutomatonFromCFG(this, it)
        }
    }

    override fun createSettings() = listOf(
        Setting(
            displayName = I18N.messages.getString("RecursiveAutomatonFactory.FromCFG"),
            editor = Button(I18N.messages.getString("RecursiveAutomatonFactory.FromCFGButton")).apply {
                action {
                    controller.getGrammar()
                }
            }
        )
    )
}