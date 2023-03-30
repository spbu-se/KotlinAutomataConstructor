package automaton.constructor.model.factory

import automaton.constructor.model.automaton.FiniteAutomaton
import automaton.constructor.model.automaton.GRAPH_PANE_CENTER
import automaton.constructor.model.element.AutomatonVertex
import automaton.constructor.model.memory.tape.InputTapeDescriptor
import automaton.constructor.model.property.FormalRegex
import automaton.constructor.utils.I18N
import automaton.constructor.utils.Setting
import com.github.h0tk3y.betterParse.parser.ParseException
import javafx.scene.control.TextField
import tornadofx.*
import java.text.MessageFormat

class FiniteAutomatonFactory : AbstractAutomatonFactory(FiniteAutomaton.DISPLAY_NAME) {
    private val regexProperty = "".toProperty()
    private var regex by regexProperty

    override fun createAutomaton() = FiniteAutomaton(inputTape = InputTapeDescriptor()).apply {
        if (regex.isNotEmpty()) {
            addTransition(
                addState(position = GRAPH_PANE_CENTER - Vector2D(AutomatonVertex.RADIUS * 10, 0.0)).apply {
                    isInitial = true
                },
                addState(position = GRAPH_PANE_CENTER + Vector2D(AutomatonVertex.RADIUS * 10, 0.0)).apply {
                    isFinal = true
                }
            ).regex = try {
                FormalRegex.fromString(regex)
            } catch (e: ParseException) {
                throw AutomatonCreationFailedException(
                    MessageFormat.format(
                        I18N.messages.getString("FiniteAutomatonFactory.FailedToParseRegex"),
                        FormalRegex.ESCAPABLE_CHARS.joinToString()
                    )
                )
            }
            undoRedoManager.reset()
        }
    }

    override fun createSettings() = listOf(
        Setting(
            displayName = I18N.messages.getString("FiniteAutomatonFactory.FromRegex"),
            editor = TextField().apply {
                regexProperty.bind(textProperty())
                promptText = I18N.messages.getString("FiniteAutomatonFactory.RegexExample")
            }
        )
    )
}
