package automaton.constructor.model.action.transition

import automaton.constructor.model.action.AbstractAction
import automaton.constructor.model.action.ActionAvailability
import automaton.constructor.model.action.ActionFailedException
import automaton.constructor.model.automaton.FiniteAutomaton
import automaton.constructor.model.automaton.copyAndAddTransition
import automaton.constructor.model.element.Transition
import automaton.constructor.model.property.EPSILON_VALUE
import automaton.constructor.model.property.FormalRegex.*
import automaton.constructor.model.property.alternatives
import automaton.constructor.model.property.concats
import automaton.constructor.utils.I18N
import tornadofx.div
import tornadofx.plus
import tornadofx.times

class SimplifyRegexTransitionAction(automaton: FiniteAutomaton) : AbstractAction<FiniteAutomaton, Transition>(
    automaton,
    I18N.messages.getString("Action.SimplifyRegexOneStep")
) {
    override fun FiniteAutomaton.doGetAvailabilityFor(actionSubject: Transition) =
        when (actionSubject.regex) {
            EPSILON_VALUE, is Singleton -> ActionAvailability.HIDDEN
            else -> ActionAvailability.AVAILABLE
        }

    override fun FiniteAutomaton.doPerformOn(actionSubject: Transition) {
        when (val regex = actionSubject.regex) {
            is KleeneStar -> {
                // uses heuristics to avoid creating unnecessary states
                if (actionSubject.source == actionSubject.target) {
                    actionSubject.regex = regex.repeated
                } else {
                    removeTransition(actionSubject)
                    val midState =
                        when {
                            getOutgoingTransitions(actionSubject.source).isEmpty() -> actionSubject.source
                            getOutgoingTransitions(actionSubject.target).isEmpty() -> actionSubject.target
                            else -> addState(
                                position = (actionSubject.source.position + actionSubject.target.position) / 2.0
                            ).also {
                                it.requiresLayout = true
                                actionSubject.source.requiresLayout = true
                                actionSubject.target.requiresLayout = true
                            }
                        }
                    if (midState !== actionSubject.source)
                        addTransition(actionSubject.source, midState).regex = EPSILON_VALUE
                    addTransition(midState, midState).regex = regex.repeated
                    if (midState !== actionSubject.target)
                        addTransition(midState, actionSubject.target).regex = EPSILON_VALUE
                }
            }

            is Concatenation -> {
                val concats = regex.concats
                when {
                    concats.isEmpty() -> actionSubject.regex = EPSILON_VALUE
                    concats.size == 1 -> actionSubject.regex = concats.single()
                    else -> {
                        actionSubject.source.requiresLayout = true
                        actionSubject.target.requiresLayout = true
                        val states = listOf(actionSubject.source) + List(concats.size - 1) { i ->
                            val targetPosFraction = (i + 1).toDouble() / concats.size
                            addState(
                                position = (1 - targetPosFraction) * actionSubject.source.position +
                                        targetPosFraction * actionSubject.target.position
                            ).apply { requiresLayout = true }
                        } + listOf(actionSubject.target)
                        concats.forEachIndexed { i, concat ->
                            addTransition(states[i], states[i + 1]).regex = concat
                        }
                        removeTransition(actionSubject)
                    }
                }
            }

            is Alternative -> {
                regex.alternatives.forEach { alternative ->
                    copyAndAddTransition(actionSubject).regex = alternative
                }
                removeTransition(actionSubject)
            }

            else -> throw ActionFailedException("Can't simplify $regex")
        }
    }
}
