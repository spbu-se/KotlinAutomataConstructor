package automaton.constructor.model.module

import automaton.constructor.model.automaton.Automaton
import automaton.constructor.utils.I18N.labels
import javafx.beans.binding.Bindings.isEmpty
import javafx.beans.binding.Bindings.isNotEmpty
import javafx.beans.value.ObservableBooleanValue
import javafx.collections.ObservableList
import tornadofx.*

private val problemDetectorFactory = { automaton: Automaton -> ProblemDetector(automaton) }
val Automaton.problemDetector get() = getModule(problemDetectorFactory)
val Automaton.problems get() = problemDetector.problems

class PotentialProblem(
    val message: String,
    val predicate: ObservableBooleanValue
)

class ProblemDetector(automaton: Automaton) : AutomatonModule {
    private val potentialProblems = observableListOf<PotentialProblem> { arrayOf(it.predicate) }
    val problems: ObservableList<PotentialProblem> = potentialProblems.filtered { it.predicate.value }

    companion object {
        val ADD_INIT_STATE_MESSAGE: String = labels.getString("ProblemDetector.ADD_INIT_STATE_MESSAGE")
        val ADD_FINAL_STATE_MESSAGE: String = labels.getString("ProblemDetector.ADD_FINAL_STATE_MESSAGE")
        val REMOVE_TRANSITIONS_FROM_FINAL_STATES_MESSAGE: String = labels.getString("ProblemDetector.REMOVE_TRANSITIONS_FROM_FINAL_STATES_MESSAGE")
    }

    init {
        potentialProblems.add(PotentialProblem(ADD_INIT_STATE_MESSAGE, isEmpty(automaton.initialStates)))
        if (automaton.memoryDescriptors.none { it.mayRequireAcceptance })
            potentialProblems.add(PotentialProblem(ADD_FINAL_STATE_MESSAGE, isEmpty(automaton.finalStates)))
        if (automaton.memoryDescriptors.all { it.isAlwaysReadyToTerminate })
            potentialProblems.add(
                PotentialProblem(
                    REMOVE_TRANSITIONS_FROM_FINAL_STATES_MESSAGE,
                    isNotEmpty(automaton.finalStatesWithTransitions)
                )
            )
    }
}
