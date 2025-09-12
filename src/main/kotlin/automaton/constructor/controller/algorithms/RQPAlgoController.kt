package automaton.constructor.controller.algorithms

import automaton.constructor.model.automaton.FiniteAutomaton
import automaton.constructor.model.element.AutomatonVertex
import automaton.constructor.model.property.FormalRegex
import automaton.constructor.utils.I18N
import automaton.constructor.view.algorithms.RPQAlgoExecutionView
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import tornadofx.Controller
import tornadofx.action
import tornadofx.observableListOf

class RPQTransition(
    val source: AutomatonVertex,
    val target: AutomatonVertex,
    var isNew: SimpleBooleanProperty = SimpleBooleanProperty(true)
) {
    override fun toString(): String = "${source.name}, ${target.name}"
}

class RPQAlgoController(private val openedAutomaton: FiniteAutomaton) : Controller() {
    var regex: FormalRegex? = null
    val regexProperty = SimpleStringProperty()

    init {
        regexProperty.addListener { _, _, newValue ->
            try {
                regex = FormalRegex.fromString(newValue)!!
            } catch (ex: Exception) {
                println("Invalid regex entered: ${ex.message}")
            }
        }
    }

    fun execute() {
        val currentTransitions = observableListOf<RPQTransition>()
        val allTransitions = observableListOf<RPQTransition>()

        val rpqView = find<RPQAlgoExecutionView>(
            mapOf(
                RPQAlgoExecutionView::currentTransitions to currentTransitions,
                RPQAlgoExecutionView::allTransitions to allTransitions,
                RPQAlgoExecutionView::regexProperty to regexProperty
            )
        ).apply {
            title = I18N.messages.getString("RPQAlgorithm.Execution.Title")
        }

        regex?.let { regexProperty.set(it.toString()) }
        rpqView.openWindow()
        rpqView.unlockRegexInput()

        rpqView.nextIterationButton.action {
            rpqView.lockRegexInput()
            TODO("not yet implemented")
        }

    }

}
