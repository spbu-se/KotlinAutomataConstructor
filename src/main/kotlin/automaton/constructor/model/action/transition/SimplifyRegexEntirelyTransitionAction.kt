package automaton.constructor.model.action.transition

import automaton.constructor.model.action.AbstractAction
import automaton.constructor.model.action.ActionAvailability.AVAILABLE
import automaton.constructor.model.automaton.FiniteAutomaton
import automaton.constructor.model.element.Transition
import automaton.constructor.utils.I18N
import javafx.collections.SetChangeListener

class SimplifyRegexEntirelyTransitionAction(automaton: FiniteAutomaton) : AbstractAction<FiniteAutomaton, Transition>(
    automaton,
    I18N.messages.getString("Action.SimplifyRegexEntirely")
) {
    private val simplifyRegexTransitionAction = SimplifyRegexTransitionAction(automaton)

    override fun FiniteAutomaton.doGetAvailabilityFor(actionSubject: Transition) =
        simplifyRegexTransitionAction.getAvailabilityFor(actionSubject)

    override fun FiniteAutomaton.doPerformOn(actionSubject: Transition) {
        val transitionQueue = ArrayDeque<Transition>()
        transitionQueue.add(actionSubject)
        val transitionsListener = SetChangeListener<Transition> {
            if (it.wasAdded()) transitionQueue.addLast(it.elementAdded)
        }
        transitions.addListener(transitionsListener)
        while (transitionQueue.isNotEmpty()) {
            val transition = transitionQueue.removeFirst()
            while (
                transition in transitions &&
                simplifyRegexTransitionAction.getAvailabilityFor(transition) == AVAILABLE
            )
                simplifyRegexTransitionAction.performOn(transition)
        }
        transitions.removeListener(transitionsListener)
    }
}
