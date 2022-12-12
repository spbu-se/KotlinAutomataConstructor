package automaton.constructor.model

import automaton.constructor.model.automaton.*
import automaton.constructor.model.data.createAutomaton
import automaton.constructor.model.data.getData
import automaton.constructor.model.serializers.JsonAutomatonSerializer
import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.div

object TestAutomatons {
    val BFS get() = getAutomatonFromJson("/bfs.atmtn") as CustomAutomaton
    val BINARY_ADDITION get() = getAutomatonFromJson("/binary-addition.atmtn") as TuringMachine
    val BINARY_INCREMENT get() = getAutomatonFromJson("/binary-increment.atmtn") as TuringMachine
    val COMPLICATED_ANY_UNARY_BUT_1_RECOGNISER get() = getAutomatonFromJson("/complicated-any-unary-but-1-recogniser.atmtn") as FiniteAutomaton
    val COMPLICATED_ANY_UNARY_BUT_1_RECOGNISER_DETERMINIZED get() = getAutomatonFromJson("/complicated-any-unary-but-1-recogniser-determinized.atmtn") as FiniteAutomaton
    val COMPLICATED_EMPTY_INPUT_DETECTOR get() = getAutomatonFromJson("/complicated-empty-input-detector.atmtn") as FiniteAutomaton
    val COMPLICATED_EMPTY_INPUT_DETECTOR_WITH_EPSILON_ELIMINATED get() = getAutomatonFromJson("/complicated-empty-input-detector-with-epsilon-eliminated.atmtn") as FiniteAutomaton
    val ELEVEN_RECOGNISER_TO_GENERATOR get() = getAutomatonFromJson("/eleven-recogniser-to-generator.atmtn") as TuringMachine
    val EMPTY_INPUT_DETECTOR_WITH_EPSILON_LOOP get() = getAutomatonFromJson("/empty-input-detector-with-epsilon-loop.atmtn") as FiniteAutomaton
    val EMPTY_INPUT_DETECTOR_WITH_EPSILON_LOOP_WITH_EPSILON_ELIMINATED get() = getAutomatonFromJson("/empty-input-detector-with-epsilon-loop-with-epsilon-eliminated.atmtn") as FiniteAutomaton
    val EVEN_PALINDROMES get() = getAutomatonFromJson("/even-palindromes.atmtn") as PushdownAutomaton
    val MEALY_REMOVE_ZEROES get() = getAutomatonFromJson("/mealy-remove-zeros.atmtn") as MealyMooreMachine
    val MEALY_REMOVE_ZEROES_CONVERTED_TO_MOORE get() = getAutomatonFromJson("/mealy-remove-zeros-converted-to-moore.atmtn") as MealyMooreMachine
    val MEALY_REMOVE_ZEROES_CONVERTED_TO_MOORE_AND_BACK get() = getAutomatonFromJson("/mealy-remove-zeros-converted-to-moore-and-back.atmtn") as MealyMooreMachine
    val MOORE_IDENTITY get() = getAutomatonFromJson("/moore-identity.atmtn") as MealyMooreMachine
    val MOORE_IDENTITY_CONVERTED_TO_MEALY get() = getAutomatonFromJson("/moore-identity-converted-to-mealy.atmtn") as MealyMooreMachine
    val MOORE_IDENTITY_CONVERTED_TO_MEALY_AND_BACK get() = getAutomatonFromJson("/moore-identity-converted-to-mealy-and-back.atmtn") as MealyMooreMachine
    val NO_FINAL_STATE get() = getAutomatonFromJson("/no-final-state.atmtn") as FiniteAutomaton
    val NO_INIT_STATE get() = getAutomatonFromJson("/no-init-state.atmtn") as FiniteAutomaton
    val NO_STATES get() = getAutomatonFromJson("/no-states.atmtn") as FiniteAutomaton
    val SINGLE_A_RECOGNISER_WITH_EPSILON_LOOPS get() = getAutomatonFromJson("/single-a-recogniser-with-epsilon-loops.atmtn") as FiniteAutomaton
    val SINGLE_A_RECOGNISER_WITH_EPSILON_LOOPS_AFTER_EPSILON_ELIMINATED get() = getAutomatonFromJson("/single-a-recogniser-with-epsilon-loops-after-epsilon-eliminated.atmtn") as FiniteAutomaton
    val SINGLE_A_RECOGNISER_WITH_EPSILON_LOOPS_AFTER_S1_S2_AND_S0_S1_EPSILON_TRANSITIONS_ELIMINATED get() = getAutomatonFromJson("/single-a-recogniser-with-epsilon-loops-after-s1-s2-and-s0-s1-epsilon-transitions-eliminated.atmtn") as FiniteAutomaton
    val SINGLE_A_RECOGNISER_WITH_EPSILON_LOOPS_AFTER_S1_S2_EPSILON_TRANSITION_ELIMINATED get() = getAutomatonFromJson("/single-a-recogniser-with-epsilon-loops-after-s1-s2-epsilon-transition-eliminated.atmtn") as FiniteAutomaton
    val SINGLE_A_RECOGNISER_WITH_EPSILON_LOOPS_AFTER_S3_S4_EPSILON_TRANSITION_ELIMINATED get() = getAutomatonFromJson("/single-a-recogniser-with-epsilon-loops-after-s3-s4-epsilon-transitions-eliminated.atmtn") as FiniteAutomaton
    val SINGLE_A_RECOGNISER_WITH_EPSILON_LOOPS_DETERMINIZED get() = getAutomatonFromJson("/single-a-recogniser-with-epsilon-loops-determinized.atmtn") as FiniteAutomaton
    val SINGLE_A_RECOGNISER_WITH_REDUNDANCY get() = getAutomatonFromJson("/single-a-recogniser-with-redundancy.atmtn") as FiniteAutomaton
    val SINGLE_A_RECOGNISER_WITH_REDUNDANCY_DETERMINIZED get() = getAutomatonFromJson("/single-a-recogniser-with-redundancy-determinized.atmtn") as FiniteAutomaton
    val THREE_ZEROES_AND_ONE_ONE get() = getAutomatonFromJson("/three-zeros-and-one-one.atmtn") as RegisterAutomaton
    val TRANSITION_FROM_FINAL_STATE get() = getAutomatonFromJson("/transition-from-final-state.atmtn") as TuringMachine

    private fun getAutomatonFromJson(path: String): Automaton {
        val file = File(requireNotNull(javaClass.getResource(path)) { "Missing resource $path" }.file)
        val tempFile = (Path(file.parent) / "temp.atmtn").toFile()
        val automaton = JsonAutomatonSerializer.deserialize(file).createAutomaton()
        JsonAutomatonSerializer.serialize(tempFile, automaton.getData()) // serialize it back to test serialization
        return JsonAutomatonSerializer.deserialize(tempFile).createAutomaton().also { tempFile.delete() }
    }
}
