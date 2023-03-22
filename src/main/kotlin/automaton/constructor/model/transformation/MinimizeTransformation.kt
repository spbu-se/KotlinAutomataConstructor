package automaton.constructor.model.transformation


import automaton.constructor.model.action.AbstractAction
import automaton.constructor.model.action.ActionAvailability
import automaton.constructor.model.action.ActionAvailability.AVAILABLE
import automaton.constructor.model.action.ActionFailedException
import automaton.constructor.model.automaton.*
import automaton.constructor.model.data.createAutomaton
import automaton.constructor.model.data.getData
import automaton.constructor.model.element.AutomatonElement
import automaton.constructor.utils.I18N

class MinimizeTransformation(
    private val input: Automaton,
    private val output: Automaton = input.getData().createAutomaton(),
) : AbstractAutomatonTransformation(input, output) {
    override val displayName = I18N.messages.getString("Minimization.Minimize")
    override val description: String? get() = null
    override val completionMessage = I18N.messages.getString("Minimization.StepByStep")

    override fun complete() = Unit
    override fun step(stepSubject: AutomatonElement) = Unit

    override fun start() {
        super.start()
        output.getDeadVertices().forEach { output.removeVertex(it) }
        output.getUnreachableVertices().forEach { output.removeVertex(it) }
        do {
            val groups = output.getNondistinguishableStateGroups()
            groups.forEach { output.mergeStates(it) }
        } while (groups.isNotEmpty())
        isCompleted = true
    }
}

class MinimizeAction(automaton: Automaton): AbstractAction<Automaton, Unit>(
    automaton,
    I18N.messages.getString("Minimization.Minimize")
) {
    override fun Automaton.doGetAvailabilityFor(actionSubject: Unit): ActionAvailability =
        AVAILABLE

    override fun Automaton.doPerformOn(actionSubject: Unit) {
        if (getDeadVertices().isEmpty() && getUnreachableVertices().isEmpty() && getNondistinguishableStateGroups().isEmpty())
            throw ActionFailedException(I18N.messages.getString("Minimization.AlreadyMinimized"))
        MinimizeTransformation(this).start()
    }
}
