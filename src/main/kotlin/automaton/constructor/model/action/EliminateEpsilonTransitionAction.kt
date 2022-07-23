package automaton.constructor.model.action

import automaton.constructor.model.State
import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.transition.Transition
import automaton.constructor.utils.I18N

class EliminateEpsilonTransitionAction(private val automaton: Automaton) : AutomatonElementAction<Transition> {
    override val displayName: String = I18N.messages.getString("AutomatonElementAction.EliminateEpsilonTransition")

    override fun isAvailableFor(element: Transition) = if (element.isPure()) {
        ActionAvailability.AVAILABLE
    } else {
        ActionAvailability.HIDDEN
    }

    override fun performOn(element: Transition) {
        fun Collection<Transition>.copyTransitions(
            source: State? = null,
            target: State? = null
        ) {
            for (transition in filterNot { it.isLoop() && it.isPure() }) {
                val (s, t) = (source ?: transition.source) to (target ?: transition.target)
                if (
                    automaton.getTransitionsFrom(s).none {
                        it.target == t && it.readProperties() == transition.readProperties()
                    }
                ) {
                    automaton.addTransition(s, t).also { newTransition ->
                        newTransition.writeProperties(transition.readProperties())
                    }
                }
            }
        }

        automaton.undoRedoManager.group {
            val source = element.source
            val target = element.target

            val transitionsToTarget = automaton.getTransitionsTo(target)
            val transitionsFromTarget = automaton.getTransitionsFrom(target)

            val transitionsToSource = automaton.getTransitionsTo(source)
            val transitionsFromSource = automaton.getTransitionsFrom(source)

            automaton.removeTransition(element)

            when {
                element.isLoop() -> Unit
                transitionsToTarget.all { it.source == source && it.isPure() } && !target.isInitial -> {
                    transitionsFromTarget.copyTransitions(source = source)
                    automaton.removeState(target)
                }
                transitionsFromSource.all { it.target == target && it.isPure() } && !source.isFinal -> {
                    transitionsToSource.copyTransitions(target = target)
                    automaton.removeState(source)
                }
                else -> transitionsFromTarget.copyTransitions(source = source)
            }
        }
    }
}
