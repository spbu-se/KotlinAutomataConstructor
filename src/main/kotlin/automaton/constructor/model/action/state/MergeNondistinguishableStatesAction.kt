package automaton.constructor.model.action.state

import automaton.constructor.model.action.AbstractAction
import automaton.constructor.model.action.ActionAvailability.AVAILABLE
import automaton.constructor.model.action.ActionAvailability.DISABLED
import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.automaton.getNondistinguishableStateGroupByMember
import automaton.constructor.model.automaton.mergeStates
import automaton.constructor.model.element.State
import automaton.constructor.utils.I18N

class MergeNondistinguishableStatesAction(automaton: Automaton) :
    AbstractAction<Automaton, State>(
        automaton = automaton,
        displayName = I18N.messages.getString("Action.MergeNondistinguishableStates")
    ) {
    override fun Automaton.doGetAvailabilityFor(actionSubject: State) =
        if (getNondistinguishableStateGroupByMember(actionSubject).isEmpty()) DISABLED
        else AVAILABLE

    override fun Automaton.doPerformOn(actionSubject: State) =
        mergeStates(getNondistinguishableStateGroupByMember(actionSubject))
}
