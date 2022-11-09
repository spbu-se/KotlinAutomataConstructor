package automaton.constructor.model.transformation

import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.automaton.resetHighlights
import automaton.constructor.model.element.AutomatonElement
import javafx.beans.property.ReadOnlyProperty
import tornadofx.*

interface AutomatonTransformation {
    val displayName: String
    val description: String?
    val resultingAutomaton: Automaton
    val isCompletedProperty: ReadOnlyProperty<Boolean>
    val isCompleted: Boolean
    fun complete()
    fun step(stepSubject: AutomatonElement)
    fun start()
    fun stop()
}

abstract class AbstractAutomatonTransformation(
    val inputAutomaton: Automaton,
    final override val resultingAutomaton: Automaton
) : AutomatonTransformation {
    final override val isCompletedProperty = false.toProperty()
    final override var isCompleted: Boolean by isCompletedProperty
        protected set

    init {
        isCompletedProperty.onChange { if (it) stop() }
    }

    override fun start() {
        inputAutomaton.isInputForTransformation = this
        resultingAutomaton.isOutputOfTransformation = this
    }

    override fun stop() {
        inputAutomaton.resetHighlights()
        resultingAutomaton.resetHighlights()
        inputAutomaton.isInputForTransformation = null
        resultingAutomaton.isOutputOfTransformation = null
    }
}
