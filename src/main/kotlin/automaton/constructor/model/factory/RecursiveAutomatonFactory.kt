package automaton.constructor.model.factory

import automaton.constructor.controller.grammar.RecursiveAutomatonGrammarInputController
import automaton.constructor.model.automaton.GRAPH_PANE_CENTER
import automaton.constructor.model.automaton.RecursiveAutomaton
import automaton.constructor.model.element.AutomatonVertex
import automaton.constructor.model.element.ContextFreeGrammar
import automaton.constructor.model.memory.tape.InputTapeDescriptor
import automaton.constructor.utils.I18N
import automaton.constructor.utils.Setting
import javafx.scene.control.Button
import tornadofx.*

class RecursiveAutomatonFactory : AbstractAutomatonFactory(RecursiveAutomaton.DISPLAY_NAME) {
    val grammarProperty = ContextFreeGrammar().toProperty()
    var grammar: ContextFreeGrammar? by grammarProperty
    val controller = RecursiveAutomatonGrammarInputController()

    override fun createAutomaton() = RecursiveAutomaton(inputTape = InputTapeDescriptor()).apply {
        if (grammar != null) {
            addTransition(
                addState(position = GRAPH_PANE_CENTER - Vector2D(AutomatonVertex.RADIUS * 10, 0.0)).apply {
                    isInitial = true
                },
                addState(position = GRAPH_PANE_CENTER + Vector2D(AutomatonVertex.RADIUS * 10, 0.0)).apply {
                    isFinal = true
                }
            )
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