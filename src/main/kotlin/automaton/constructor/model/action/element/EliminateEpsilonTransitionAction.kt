package automaton.constructor.model.action.element

import automaton.constructor.model.State
import automaton.constructor.model.action.ActionAvailability.AVAILABLE
import automaton.constructor.model.action.ActionAvailability.HIDDEN
import automaton.constructor.model.action.createAutomatonElementAction
import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.automaton.copyAndAddTransition
import automaton.constructor.model.transition.Transition
import automaton.constructor.utils.I18N

fun createEliminateEpsilonTransitionAction(automaton: Automaton) =
    createAutomatonElementAction<Automaton, Transition>(
        automaton = automaton,
        displayName = I18N.messages.getString("AutomatonElementAction.EliminateEpsilonTransition"),
        getAvailabilityFor = { transition -> if (transition.isPure()) AVAILABLE else HIDDEN },
        performOn = { transition ->
            fun Iterable<Transition>.copyAndAddTransitions(newSource: State? = null, newTarget: State? = null) =
                forEach { transitionToCopy ->
                    copyAndAddTransition(
                        transitionToCopy, newSource, newTarget,
                        ignoreIfTransitionIsPureLoop = true,
                        ignoreIfCopyIsPureLoop = true,
                        ignoreIfCopyAlreadyExists = true
                    )
                }

            val source = transition.source
            val target = transition.target

            val targetOutgoingTransitions = getOutgoingTransitions(target)
            val sourceIncomingTransitions = getIncomingTransitions(source)

            removeTransition(transition)

            if (source.isInitial) target.isInitial = true
            if (target.isFinal) source.isFinal = true

            if (!transition.isLoop()) {
                if (
                    targetOutgoingTransitions.any { !it.isLoop() && it.target != source } ||
                    sourceIncomingTransitions.none { !it.isLoop() && it.source != target } &&
                    sourceIncomingTransitions.any { it.isLoop() }
                ) {
                    targetOutgoingTransitions.copyAndAddTransitions(newSource = source)
                } else {
                    sourceIncomingTransitions.copyAndAddTransitions(newTarget = target)
                }
            }
        }
    )
