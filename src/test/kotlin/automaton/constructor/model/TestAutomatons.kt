package automaton.constructor.model

import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.automaton.CustomAutomaton
import automaton.constructor.model.automaton.FiniteAutomaton
import automaton.constructor.model.automaton.MealyMooreMachine
import automaton.constructor.model.automaton.PushdownAutomaton
import automaton.constructor.model.automaton.RegisterAutomaton
import automaton.constructor.model.automaton.TuringMachine
import automaton.constructor.model.data.createAutomaton
import automaton.constructor.model.serializers.JsonAutomatonSerializer
import java.io.File
import java.util.stream.Stream

object TestAutomatons {
    val ALTERNATIVE get() = getAutomatonFromJson("/alternative.atmtn") as FiniteAutomaton
    val ALTERNATIVE_SIMPLIFIED get() = getAutomatonFromJson("/alternative-simplified.atmtn") as FiniteAutomaton
    val BFS get() = getAutomatonFromJson("/bfs.atmtn") as CustomAutomaton
    val BINARY_ADDITION get() = getAutomatonFromJson("/binary-addition.atmtn") as TuringMachine
    val BINARY_INCREMENT get() = getAutomatonFromJson("/binary-increment.atmtn") as TuringMachine
    val C_ALIAS_DFA get() = getAutomatonFromJson("/c-alias-dfa.atmtn") as FiniteAutomaton
    val C_ALIAS_MIN_DFA get() = getAutomatonFromJson("/c-alias-min-dfa.atmtn") as FiniteAutomaton
    val C_ALIAS_NFA get() = getAutomatonFromJson("/c-alias-nfa.atmtn") as FiniteAutomaton
    val C_ALIAS_REGEX get() = getAutomatonFromJson("/c-alias-regex.atmtn") as FiniteAutomaton
    val COMPLICATED_ANY_UNARY_BUT_1_RECOGNISER get() = getAutomatonFromJson("/complicated-any-unary-but-1-recogniser.atmtn") as FiniteAutomaton
    val COMPLICATED_ANY_UNARY_BUT_1_RECOGNISER_DETERMINIZED get() = getAutomatonFromJson("/complicated-any-unary-but-1-recogniser-determinized.atmtn") as FiniteAutomaton
    val COMPLICATED_EMPTY_INPUT_DETECTOR get() = getAutomatonFromJson("/complicated-empty-input-detector.atmtn") as FiniteAutomaton
    val COMPLICATED_EMPTY_INPUT_DETECTOR_WITH_EPSILON_ELIMINATED get() = getAutomatonFromJson("/complicated-empty-input-detector-with-epsilon-eliminated.atmtn") as FiniteAutomaton
    val CONCAT get() = getAutomatonFromJson("/concat.atmtn") as FiniteAutomaton
    val CONCAT_DOUBLE_EPSILON get() = getAutomatonFromJson("/concat-double-epsilon.atmtn") as FiniteAutomaton
    val CONCAT_DOUBLE_EPSILON_SIMPLIFIED get() = getAutomatonFromJson("/concat-double-epsilon-simplified.atmtn") as FiniteAutomaton
    val CONCAT_EPSILON get() = getAutomatonFromJson("/concat-epsilon.atmtn") as FiniteAutomaton
    val CONCAT_EPSILON_SIMPLIFIED get() = getAutomatonFromJson("/concat-epsilon-simplified.atmtn") as FiniteAutomaton
    val CONCAT_SIMPLIFIED get() = getAutomatonFromJson("/concat-simplified.atmtn") as FiniteAutomaton
    val DEAD_STATE get() = getAutomatonFromJson("/dead-state.atmtn") as FiniteAutomaton
    val ELEVEN_RECOGNISER_TO_GENERATOR get() = getAutomatonFromJson("/eleven-recogniser-to-generator.atmtn") as TuringMachine
    val EMPTY_INPUT_DETECTOR_WITH_EPSILON_LOOP get() = getAutomatonFromJson("/empty-input-detector-with-epsilon-loop.atmtn") as FiniteAutomaton
    val EMPTY_INPUT_DETECTOR_WITH_EPSILON_LOOP_WITH_EPSILON_ELIMINATED get() = getAutomatonFromJson("/empty-input-detector-with-epsilon-loop-with-epsilon-eliminated.atmtn") as FiniteAutomaton
    val EPSILON get() = getAutomatonFromJson("/epsilon.atmtn") as FiniteAutomaton
    val EVEN_PALINDROMES get() = getAutomatonFromJson("/even-palindromes.atmtn") as PushdownAutomaton
    val KLEENE_STAR get() = getAutomatonFromJson("/kleene-star.atmtn") as FiniteAutomaton
    val KLEENE_STAR_LOOP get() = getAutomatonFromJson("/kleene-star-loop.atmtn") as FiniteAutomaton
    val KLEENE_STAR_LOOP_SIMPLIFIED get() = getAutomatonFromJson("/kleene-star-loop-simplified.atmtn") as FiniteAutomaton
    val KLENEE_STAR_SIMPLIFIED get() = getAutomatonFromJson("/klenee-star-simplified.atmtn") as FiniteAutomaton
    val MEALY_REMOVE_ZEROES get() = getAutomatonFromJson("/mealy-remove-zeros.atmtn") as MealyMooreMachine
    val MEALY_REMOVE_ZEROES_CONVERTED_TO_MOORE get() = getAutomatonFromJson("/mealy-remove-zeros-converted-to-moore.atmtn") as MealyMooreMachine
    val MEALY_REMOVE_ZEROES_CONVERTED_TO_MOORE_AND_BACK get() = getAutomatonFromJson("/mealy-remove-zeros-converted-to-moore-and-back.atmtn") as MealyMooreMachine
    val MOORE_IDENTITY get() = getAutomatonFromJson("/moore-identity.atmtn") as MealyMooreMachine
    val MOORE_IDENTITY_CONVERTED_TO_MEALY get() = getAutomatonFromJson("/moore-identity-converted-to-mealy.atmtn") as MealyMooreMachine
    val MOORE_IDENTITY_CONVERTED_TO_MEALY_AND_BACK get() = getAutomatonFromJson("/moore-identity-converted-to-mealy-and-back.atmtn") as MealyMooreMachine
    val NO_FINAL_STATE get() = getAutomatonFromJson("/no-final-state.atmtn") as FiniteAutomaton
    val NO_INIT_STATE get() = getAutomatonFromJson("/no-init-state.atmtn") as FiniteAutomaton
    val NO_STATES get() = getAutomatonFromJson("/no-states.atmtn") as FiniteAutomaton
    val NONDISTINGUISHABLE_STATES get() = getAutomatonFromJson("/nondistinguishable-states.atmtn") as FiniteAutomaton
    val NONDISTINGUISHABLE_STATES_MERGE_S0 get() = getAutomatonFromJson("/nondistinguishable-states-merge-s0.atmtn") as FiniteAutomaton
    val NONDISTINGUISHABLE_STATES_MERGE_S1 get() = getAutomatonFromJson("/nondistinguishable-states-merge-s1.atmtn") as FiniteAutomaton
    val SINGLE_A_RECOGNISER_WITH_EPSILON_LOOPS get() = getAutomatonFromJson("/single-a-recogniser-with-epsilon-loops.atmtn") as FiniteAutomaton
    val SINGLE_A_RECOGNISER_WITH_EPSILON_LOOPS_AFTER_EPSILON_ELIMINATED get() = getAutomatonFromJson("/single-a-recogniser-with-epsilon-loops-after-epsilon-eliminated.atmtn") as FiniteAutomaton
    val SINGLE_A_RECOGNISER_WITH_EPSILON_LOOPS_AFTER_S1_S2_AND_S0_S1_EPSILON_TRANSITIONS_ELIMINATED get() = getAutomatonFromJson("/single-a-recogniser-with-epsilon-loops-after-s1-s2-and-s0-s1-epsilon-transitions-eliminated.atmtn") as FiniteAutomaton
    val SINGLE_A_RECOGNISER_WITH_EPSILON_LOOPS_AFTER_S1_S2_EPSILON_TRANSITION_ELIMINATED get() = getAutomatonFromJson("/single-a-recogniser-with-epsilon-loops-after-s1-s2-epsilon-transition-eliminated.atmtn") as FiniteAutomaton
    val SINGLE_A_RECOGNISER_WITH_EPSILON_LOOPS_AFTER_S3_S4_EPSILON_TRANSITION_ELIMINATED get() = getAutomatonFromJson("/single-a-recogniser-with-epsilon-loops-after-s3-s4-epsilon-transitions-eliminated.atmtn") as FiniteAutomaton
    val SINGLE_A_RECOGNISER_WITH_EPSILON_LOOPS_DETERMINIZED get() = getAutomatonFromJson("/single-a-recogniser-with-epsilon-loops-determinized.atmtn") as FiniteAutomaton
    val SINGLE_A_RECOGNISER_WITH_REDUNDANCY get() = getAutomatonFromJson("/single-a-recogniser-with-redundancy.atmtn") as FiniteAutomaton
    val SINGLE_A_RECOGNISER_WITH_REDUNDANCY_DETERMINIZED get() = getAutomatonFromJson("/single-a-recogniser-with-redundancy-determinized.atmtn") as FiniteAutomaton
    val SINGLE_A get() = getAutomatonFromJson("/single-a.atmtn") as FiniteAutomaton
    val THREE_ZEROES_AND_ONE_ONE get() = getAutomatonFromJson("/three-zeros-and-one-one.atmtn") as RegisterAutomaton
    val TRANSITION_FROM_FINAL_STATE get() = getAutomatonFromJson("/transition-from-final-state.atmtn") as TuringMachine
    val UNREACH_STATE get() = getAutomatonFromJson("/unreach-state.atmtn") as FiniteAutomaton
    val USELESS_STATE_REMOVED get() = getAutomatonFromJson("/useless-state-removed.atmtn") as FiniteAutomaton
    val FROM_REFERENCE_COURSE get() = getAutomatonFromJson("/from-reference-course.atmtn") as FiniteAutomaton
    val CAACBB get() = getAutomatonFromJson("/caacbb.atmtn") as FiniteAutomaton
    val DFA_110011 get() = getAutomatonFromJson("/dfa-110011.atmtn") as FiniteAutomaton
    val CORRECT_BRACKET_SEQUENCE_RECOGNISER get() = getAutomatonFromJson("/correct-bracket-sequence-recogniser.atmtn") as PushdownAutomaton
    val SAME_NUMBER_OF_ZEROS_AND_ONES get() = getAutomatonFromJson("/same-number-of-zeros-and-ones.atmtn") as PushdownAutomaton
    val DFA_0110011 get() = getAutomatonFromJson("/dfa-0110011.atmtn") as FiniteAutomaton
    val SAME_NUMBER_OF_ZEROS_AND_ONES_BY_EMPTY_STACK get() = getAutomatonFromJson("/same-number-of-zeros-and-ones-by-empty-stack.atmtn") as PushdownAutomaton

    private fun getAutomatonFromJson(path: String): Automaton {
        val file = File(requireNotNull(javaClass.getResource(path)) { "Missing resource $path" }.file)
        return JsonAutomatonSerializer.deserialize(file).createAutomaton()
    }

    fun allAutomataStream(): Stream<Automaton> = Stream.of(
        ALTERNATIVE,
        ALTERNATIVE_SIMPLIFIED,
        BFS,
        BINARY_ADDITION,
        BINARY_INCREMENT,
        C_ALIAS_DFA,
        C_ALIAS_MIN_DFA,
        C_ALIAS_NFA,
        C_ALIAS_REGEX,
        COMPLICATED_ANY_UNARY_BUT_1_RECOGNISER,
        COMPLICATED_ANY_UNARY_BUT_1_RECOGNISER_DETERMINIZED,
        COMPLICATED_EMPTY_INPUT_DETECTOR,
        COMPLICATED_EMPTY_INPUT_DETECTOR_WITH_EPSILON_ELIMINATED,
        CONCAT,
        CONCAT_DOUBLE_EPSILON,
        CONCAT_DOUBLE_EPSILON_SIMPLIFIED,
        CONCAT_EPSILON,
        CONCAT_EPSILON_SIMPLIFIED,
        CONCAT_SIMPLIFIED,
        DEAD_STATE,
        ELEVEN_RECOGNISER_TO_GENERATOR,
        EMPTY_INPUT_DETECTOR_WITH_EPSILON_LOOP,
        EMPTY_INPUT_DETECTOR_WITH_EPSILON_LOOP_WITH_EPSILON_ELIMINATED,
        EPSILON,
        EVEN_PALINDROMES,
        KLEENE_STAR,
        KLEENE_STAR_LOOP,
        KLEENE_STAR_LOOP_SIMPLIFIED,
        KLENEE_STAR_SIMPLIFIED,
        MEALY_REMOVE_ZEROES,
        MEALY_REMOVE_ZEROES_CONVERTED_TO_MOORE,
        MEALY_REMOVE_ZEROES_CONVERTED_TO_MOORE_AND_BACK,
        MOORE_IDENTITY,
        MOORE_IDENTITY_CONVERTED_TO_MEALY,
        MOORE_IDENTITY_CONVERTED_TO_MEALY_AND_BACK,
        NO_FINAL_STATE,
        NO_INIT_STATE,
        NO_STATES,
        NONDISTINGUISHABLE_STATES,
        NONDISTINGUISHABLE_STATES_MERGE_S0,
        NONDISTINGUISHABLE_STATES_MERGE_S1,
        SINGLE_A_RECOGNISER_WITH_EPSILON_LOOPS,
        SINGLE_A_RECOGNISER_WITH_EPSILON_LOOPS_AFTER_EPSILON_ELIMINATED,
        SINGLE_A_RECOGNISER_WITH_EPSILON_LOOPS_AFTER_S1_S2_AND_S0_S1_EPSILON_TRANSITIONS_ELIMINATED,
        SINGLE_A_RECOGNISER_WITH_EPSILON_LOOPS_AFTER_S1_S2_EPSILON_TRANSITION_ELIMINATED,
        SINGLE_A_RECOGNISER_WITH_EPSILON_LOOPS_AFTER_S3_S4_EPSILON_TRANSITION_ELIMINATED,
        SINGLE_A_RECOGNISER_WITH_EPSILON_LOOPS_DETERMINIZED,
        SINGLE_A_RECOGNISER_WITH_REDUNDANCY,
        SINGLE_A_RECOGNISER_WITH_REDUNDANCY_DETERMINIZED,
        SINGLE_A,
        THREE_ZEROES_AND_ONE_ONE,
        TRANSITION_FROM_FINAL_STATE,
        UNREACH_STATE,
        USELESS_STATE_REMOVED,
        FROM_REFERENCE_COURSE,
        CAACBB,
        DFA_110011,
        CORRECT_BRACKET_SEQUENCE_RECOGNISER,
        SAME_NUMBER_OF_ZEROS_AND_ONES,
        DFA_0110011,
        SAME_NUMBER_OF_ZEROS_AND_ONES_BY_EMPTY_STACK
    )
}
