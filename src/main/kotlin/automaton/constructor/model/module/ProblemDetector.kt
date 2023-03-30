package automaton.constructor.model.module

import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.memory.AcceptanceRequiringPolicy.NEVER
import automaton.constructor.utils.I18N.messages
import automaton.constructor.utils.filteredSet
import javafx.beans.binding.Bindings.isEmpty
import javafx.beans.binding.Bindings.isNotEmpty
import javafx.beans.binding.BooleanBinding
import javafx.beans.value.ObservableBooleanValue
import javafx.collections.ObservableList
import tornadofx.observableListOf

private val problemDetectorFactory = { automaton: Automaton -> ProblemDetector(automaton) }
val Automaton.problemDetector get() = getModule(problemDetectorFactory)
val Automaton.problems get() = problemDetector.problems
val Automaton.hasProblemsBinding get() = problemDetector.hasProblemsBinding
val Automaton.hasProblems: Boolean get() = hasProblemsBinding.value

class PotentialProblem(
    val message: String,
    val predicate: ObservableBooleanValue
)

class ProblemDetector(automaton: Automaton) : AutomatonModule {
    private val potentialProblems = observableListOf<PotentialProblem> { arrayOf(it.predicate) }
    val problems: ObservableList<PotentialProblem> = potentialProblems.filtered { it.predicate.value }
    val hasProblemsBinding: BooleanBinding = isNotEmpty(problems)

    companion object {
        val ADD_INIT_STATE_MESSAGE: String = messages.getString("ProblemDetector.AddInitState")
        val ADD_FINAL_STATE_MESSAGE: String = messages.getString("ProblemDetector.AddFinalState")
        val REMOVE_TRANSITIONS_FROM_FINAL_STATES_MESSAGE: String =
            messages.getString("ProblemDetector.RemoveTransitionsFromFinalStates")
        val FIX_PROBLEMS_IN_BUILDING_BLOCKS_MESSAGE: String =
            messages.getString("ProblemDetector.FixProblemsInRedBuildingBlocks")
        val SIMPLIFY_REGEXES_USING_TRANSITION_CONTEXT_ACTION_MESSAGE: String =
            messages.getString("ProblemDetector.SimplifyRegexesUsingTransitionContextAction")
    }

    init {
        potentialProblems.add(PotentialProblem(ADD_INIT_STATE_MESSAGE, isEmpty(automaton.initialVertices)))
        if (automaton.memoryDescriptors.all { it.acceptanceRequiringPolicy == NEVER })
            potentialProblems.add(PotentialProblem(ADD_FINAL_STATE_MESSAGE, isEmpty(automaton.finalVertices)))
        if (automaton.memoryDescriptors.all { it.isAlwaysReadyToTerminate })
            potentialProblems.add(
                PotentialProblem(
                    REMOVE_TRANSITIONS_FROM_FINAL_STATES_MESSAGE,
                    isNotEmpty(automaton.finalVerticesWithTransitions)
                )
            )
        potentialProblems.add(
            PotentialProblem(
                FIX_PROBLEMS_IN_BUILDING_BLOCKS_MESSAGE,
                isNotEmpty(automaton.buildingBlocks.filteredSet { it.subAutomaton.hasProblemsBinding })
            )
        )
        potentialProblems.add(
            PotentialProblem(
                SIMPLIFY_REGEXES_USING_TRANSITION_CONTEXT_ACTION_MESSAGE,
                automaton.hasRegexesBinding
            )
        )
    }
}
