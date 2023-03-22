package automaton.constructor.model.transformation

import automaton.constructor.model.action.Action
import automaton.constructor.model.action.ActionAvailability
import automaton.constructor.model.action.ActionAvailability.AVAILABLE
import automaton.constructor.model.action.ActionAvailability.DISABLED
import automaton.constructor.model.action.ActionFailedException
import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.data.createAutomaton
import automaton.constructor.model.data.getData
import automaton.constructor.model.element.AutomatonElement
import automaton.constructor.model.element.State
import automaton.constructor.utils.I18N
import javafx.scene.input.KeyCombination
import java.text.MessageFormat

class StatewiseTransformation(
    override val displayName: String,
    private val input: Automaton,
    private val output: Automaton = input.getData().createAutomaton(),
    stateActionFinder: (Automaton) -> Action<State>,
) : AbstractAutomatonTransformation(input, output) {
    private val stateAction = stateActionFinder(output)
    override val description: String? get() = null
    override val completionMessage: String = MessageFormat.format(
        I18N.messages.getString("StatewiseTransformation.StepByStep"),
        stateAction.displayName
    )

    override fun complete() = Unit
    override fun step(stepSubject: AutomatonElement) = Unit

    override fun start() {
        super.start()
        output.states
            .toList() // copy list to avoid ConcurrentModificationException
            .forEach { state ->
                if (stateAction.getAvailabilityFor(state) == AVAILABLE)
                    stateAction.performOn(state)
            }
        isCompleted = true
    }
}

class StatewiseTransformationAction(
    override val displayName: String,
    val automaton: Automaton,
    private val unavailableMessage: String,
    override val keyCombination: KeyCombination? = null,
    private val stateActionFinder: (Automaton) -> Action<State>
) : Action<Unit> {
    override fun getAvailabilityFor(actionSubject: Unit): ActionAvailability {
        val stateAction = stateActionFinder(automaton)
        return if (automaton.states.any { stateAction.getAvailabilityFor(it) == AVAILABLE }) AVAILABLE
        else DISABLED
    }

    override fun performOn(actionSubject: Unit) {
        if (getAvailabilityFor(actionSubject) != AVAILABLE) throw ActionFailedException(unavailableMessage)
        StatewiseTransformation(displayName, automaton, stateActionFinder = stateActionFinder).start()
    }
}
