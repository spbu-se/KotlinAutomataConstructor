package automaton.constructor.model.module

import automaton.constructor.model.Automaton
import javafx.beans.binding.Bindings.isEmpty
import javafx.beans.binding.Bindings.isNotEmpty
import javafx.beans.value.ObservableBooleanValue
import tornadofx.*

val problemDetectorFactory = { automaton: Automaton -> ProblemDetector(automaton) }
val Automaton.problemDetector get() = getModule(problemDetectorFactory)
val Automaton.problems get() = problemDetector.problems

data class Problem(val message: String)

val NO_INIT_STATE_PROBLEM = Problem("Add initial state")
val NO_FINAL_STATE_PROBLEM = Problem("Add final state")
val FINAL_STATE_WITH_TRANSITION_PROBLEM = Problem("Remove transitions from final states")

class ProblemDetector(val automaton: Automaton) : AutomatonModule {
    val problems = observableListOf<Problem>()

    init {
        registerPotentialProblem(NO_INIT_STATE_PROBLEM, isEmpty(automaton.initialStates))
        if (automaton.memoryDescriptors.none { it.mayRequireAcceptance })
            registerPotentialProblem(NO_FINAL_STATE_PROBLEM, isEmpty(automaton.finalStates))
        if (automaton.memoryDescriptors.all { it.isAlwaysReadyToTerminate })
            registerPotentialProblem(
                FINAL_STATE_WITH_TRANSITION_PROBLEM,
                isNotEmpty(automaton.finalStatesWithTransitions)
            )
    }

    private fun registerPotentialProblem(problem: Problem, predicate: ObservableBooleanValue) {
        if (predicate.value) problems.add(problem)
        predicate.onChange { if (it) problems.add(problem) else problems.remove(problem) }
    }
}
