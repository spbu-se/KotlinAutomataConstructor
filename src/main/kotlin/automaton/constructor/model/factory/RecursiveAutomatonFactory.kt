package automaton.constructor.model.factory

import automaton.constructor.controller.grammar.RecursiveAutomatonGrammarInputController
import automaton.constructor.model.automaton.RecursiveAutomaton
import automaton.constructor.model.element.EBNFGrammar
import automaton.constructor.model.memory.tape.InputTapeDescriptor
import automaton.constructor.utils.I18N
import automaton.constructor.utils.Setting
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.Button
import tornadofx.action
import tornadofx.getValue
import tornadofx.setValue

class RecursiveAutomatonFactory : AbstractAutomatonFactory(RecursiveAutomaton.DISPLAY_NAME) {
    val grammarProperty = SimpleObjectProperty<EBNFGrammar?>(null)

    var grammar: EBNFGrammar? by grammarProperty

    var lastCreatedAutomaton: RecursiveAutomaton? = null
    val controller = RecursiveAutomatonGrammarInputController(this)

    override fun createAutomaton() = RecursiveAutomaton(inputTape = InputTapeDescriptor()).apply {
        val cfg = this@RecursiveAutomatonFactory.grammar
        lastCreatedAutomaton = this
        if (cfg != null) {
            this.grammar = cfg
            runCatching { this.name = cfg.initialNonterminal.value }
            controller.buildRecursiveAutomatonFromCFG(this, cfg)
            runCatching { this.name = cfg.initialNonterminal.value }
        }
    }

    override fun createSettings() = listOf(
        Setting(
            displayName = I18N.messages.getString("RecursiveAutomatonFactory.FromCFG"),
            editor = Button(I18N.messages.getString("RecursiveAutomatonFactory.FromCFGButton")).apply {
                action { controller.getGrammar() }
            })
    )
}