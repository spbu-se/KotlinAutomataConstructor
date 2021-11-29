package automaton.constructor.model.module

import automaton.constructor.model.Automaton
import javafx.collections.SetChangeListener
import tornadofx.*

val problemDetectorFactory = { automaton: Automaton -> ProblemDetector(automaton) }
val Automaton.problemDetector get() = getModule(problemDetectorFactory)
val Automaton.problems get() = problemDetector.problems

data class Problem(val message: String)

val NO_INIT_STATE_PROBLEM = Problem("Add initial state")
val NO_FINAL_STATE_PROBLEM = Problem("Add final state")

class ProblemDetector(val automaton: Automaton) : AutomatonModule {
    val problems = observableListOf<Problem>()

    init {
        fun updateNoInitStateProblem() {
            if (automaton.initialStates.isEmpty()) problems.add(NO_INIT_STATE_PROBLEM)
            else problems.remove(NO_INIT_STATE_PROBLEM)
        }
        updateNoInitStateProblem()
        automaton.initialStates.addListener(SetChangeListener { updateNoInitStateProblem() })

        fun updateNoFinalStateProblem() {
            if (automaton.finalStates.isEmpty()) problems.add(NO_FINAL_STATE_PROBLEM)
            else problems.remove(NO_FINAL_STATE_PROBLEM)
        }
        updateNoFinalStateProblem()
        automaton.finalStates.addListener(SetChangeListener { updateNoFinalStateProblem() })
    }
}
