package automaton.constructor.model.action.transition

import automaton.constructor.model.action.AbstractAction
import automaton.constructor.model.action.ActionAvailability
import automaton.constructor.model.automaton.RecursiveAutomaton
import automaton.constructor.model.element.Transition
import automaton.constructor.utils.I18N

class ConvertCFGToRecursiveAutomatonAction(automaton: RecursiveAutomaton):
    AbstractAction<RecursiveAutomaton, Transition>(automaton, displayName = I18N.messages.getString("Action.ConvertCFGToRecursiveAutomaton")) {
    override fun RecursiveAutomaton.doGetAvailabilityFor(actionSubject: Transition): ActionAvailability {
        TODO("Not yet implemented")
    }

    override fun RecursiveAutomaton.doPerformOn(actionSubject: Transition) {
        TODO("Not yet implemented")
    }
}