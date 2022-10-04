package automaton.constructor.model.action.transition

import automaton.constructor.model.action.ActionAvailability.*
import automaton.constructor.model.action.createAutomatonElementAction
import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.automaton.copyAndAddTransition
import automaton.constructor.model.element.AutomatonVertex
import automaton.constructor.model.element.State
import automaton.constructor.model.element.Transition
import automaton.constructor.utils.I18N

fun createEliminateEpsilonTransitionAction(automaton: Automaton) =
    createAutomatonElementAction<Automaton, Transition>(
        automaton = automaton,
        displayName = I18N.messages.getString("AutomatonElementAction.EliminateEpsilonTransition"),
        getAvailabilityFor = { transition ->
            when {
                !transition.isPure() -> HIDDEN
                transition.source is State && transition.target is State -> AVAILABLE
                else -> DISABLED
            }
        },
        performOn = { transition ->
            fun Iterable<Transition>.copyAndAddTransitions(
                newSource: AutomatonVertex? = null,
                newTarget: AutomatonVertex? = null
            ) =
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
                    if ((source.isInitial || !target.isInitial) && getIncomingTransitions(target).isEmpty())
                        removeVertex(target)
                } else {
                    sourceIncomingTransitions.copyAndAddTransitions(newTarget = target)
                    if ((target.isFinal || !source.isFinal) && getOutgoingTransitions(source).isEmpty())
                        removeVertex(source)
                }
            }
        }
    )
