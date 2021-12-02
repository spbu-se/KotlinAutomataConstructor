package automaton.constructor.model.module

import automaton.constructor.model.Automaton
import javafx.beans.binding.Bindings.isEmpty
import javafx.beans.binding.Bindings.isNotEmpty
import javafx.beans.value.ObservableBooleanValue
import javafx.collections.ObservableList
import tornadofx.*

val problemDetectorFactory = { automaton: Automaton -> ProblemDetector(automaton) }
val Automaton.problemDetector get() = getModule(problemDetectorFactory)
val Automaton.problems get() = problemDetector.problems

class PotentialProblem(
    val message: String,
    val predicate: ObservableBooleanValue
)

class ProblemDetector(val automaton: Automaton) : AutomatonModule {
    private val potentialProblems = observableListOf<PotentialProblem> { arrayOf(it.predicate) }
    val problems: ObservableList<PotentialProblem> = potentialProblems.filtered { it.predicate.value }

    init {
        potentialProblems.add(PotentialProblem("Add initial state", isEmpty(automaton.initialStates)))
        if (automaton.memoryDescriptors.none { it.mayRequireAcceptance })
            potentialProblems.add(PotentialProblem("Add final state", isEmpty(automaton.finalStates)))
        if (automaton.memoryDescriptors.all { it.isAlwaysReadyToTerminate })
            potentialProblems.add(
                PotentialProblem(
                    "Remove transitions from final states",
                    isNotEmpty(automaton.finalStatesWithTransitions)
                )
            )
    }
}
