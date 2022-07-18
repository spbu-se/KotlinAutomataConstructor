package automaton.constructor.model.module.executor

import automaton.constructor.model.TestAutomatons
import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.memory.RegisterDescriptor
import automaton.constructor.model.memory.output.OutputTape
import automaton.constructor.model.memory.tape.InputTapeDescriptor
import automaton.constructor.model.memory.tape.MultiTrackTape
import automaton.constructor.model.memory.tape.MultiTrackTapeDescriptor
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.assertEquals

class ExecutorTest {
    companion object {
        const val MAX_RUN_MILLIS = 100L
    }

    @Nested
    inner class BFS {
        private lateinit var automaton: Automaton

        @BeforeEach
        fun init() {
            automaton = TestAutomatons.BFS
        }

        @Test
        fun `should accept`() = assertEquals(ExecutionStatus.ACCEPTED, getResult())

        private fun getResult(): ExecutionStatus {
            automaton.executor.start()
            automaton.executor.runFor(MAX_RUN_MILLIS)
            return automaton.executor.status
        }
    }

    @Nested
    inner class BinaryIncrement {
        private lateinit var automaton: Automaton
        private lateinit var tapeDescriptor: MultiTrackTapeDescriptor

        @BeforeEach
        fun init() {
            automaton = TestAutomatons.BINARY_INCREMENT
            tapeDescriptor = automaton.memoryDescriptors[0] as MultiTrackTapeDescriptor
        }

        @ParameterizedTest
        @CsvSource("1011,1100", "1111,10000", ",1", "0,1")
        fun test(input: String?, output: String) = assertEquals(output, getResult(input ?: ""))

        @Test
        fun `when root execution state is frozen then executor should freeze`() {
            automaton.executor.start()
            automaton.executor.roots.first().isFrozen = true
            automaton.executor.runFor(MAX_RUN_MILLIS)
            assertEquals(ExecutionStatus.FROZEN, automaton.executor.status)
        }

        private fun getResult(input: String): String {
            tapeDescriptor.valueProperties[0].value = input
            automaton.executor.start()
            automaton.executor.runFor(MAX_RUN_MILLIS)
            val track = (automaton.executor.acceptedStates.first().memory[0] as MultiTrackTape).tracks[0]
            return track.processed + track.current + track.unprocessed
        }
    }

    @Nested
    inner class EmptyInputDetectorWithEpsilonLoop {
        private lateinit var automaton: Automaton
        private lateinit var tapeDescriptor: InputTapeDescriptor

        @BeforeEach
        fun init() {
            automaton = TestAutomatons.EMPTY_INPUT_DETECTOR_WITH_EPSILON_LOOP
            tapeDescriptor = automaton.memoryDescriptors[0] as InputTapeDescriptor
        }

        @ParameterizedTest
        @CsvSource(",ACCEPTED", "0,RUNNING")
        fun `test step by state`(input: String?, output: ExecutionStatus) =
            assertEquals(output, getResult(input ?: "", STEP_BY_STATE_STRATEGY))

        @ParameterizedTest
        @CsvSource(",ACCEPTED", "0,REJECTED")
        fun `test step by closure`(input: String?, output: ExecutionStatus) =
            assertEquals(output, getResult(input ?: "", STEP_BY_CLOSURE_STRATEGY))

        private fun getResult(input: String, steppingStrategy: SteppingStrategy): ExecutionStatus {
            tapeDescriptor.value = input
            automaton.executor.start()
            automaton.executor.runFor(MAX_RUN_MILLIS, steppingStrategy)
            return automaton.executor.status
        }
    }

    @Nested
    inner class EvenPalindromes {
        private lateinit var automaton: Automaton
        private lateinit var tapeDescriptor: InputTapeDescriptor

        @BeforeEach
        fun init() {
            automaton = TestAutomatons.EVEN_PALINDROMES
            tapeDescriptor = automaton.memoryDescriptors[0] as InputTapeDescriptor
        }

        @ParameterizedTest
        @CsvSource("110011,ACCEPTED", "100011,REJECTED", "11011,REJECTED", ",ACCEPTED")
        fun test(input: String?, output: ExecutionStatus) = assertEquals(output, getResult(input ?: ""))

        @Test
        fun `when root is collapsed after execution then root execution state should be the only execution state`() {
            tapeDescriptor.value = "110011"
            automaton.executor.start()
            val root = automaton.executor.roots.first()
            automaton.executor.runFor(MAX_RUN_MILLIS)
            root.collapse()
            assertEquals(setOf(root), automaton.executor.executionStates)
        }

        private fun getResult(input: String): ExecutionStatus {
            tapeDescriptor.value = input
            automaton.executor.start()
            automaton.executor.runFor(MAX_RUN_MILLIS)
            return automaton.executor.status
        }
    }

    @Nested
    inner class MealyRemoveZeroes : AbstractOutputTape() {
        override fun createAutomaton() = TestAutomatons.MEALY_REMOVE_ZEROES

        override fun getInputOutputPairs() = listOf("" to "", "001" to "1", "11" to "11", "0" to "")
    }

    @Nested
    inner class MooreIdentity : AbstractOutputTape() {
        override fun createAutomaton() = TestAutomatons.MOORE_IDENTITY

        override fun getInputOutputPairs() = listOf("" to "", "001" to "001")
    }

    abstract inner class AbstractOutputTape {
        private lateinit var automaton: Automaton
        private lateinit var tapeDescriptor: InputTapeDescriptor

        protected abstract fun createAutomaton(): Automaton

        @BeforeEach
        fun init() {
            automaton = createAutomaton()
            tapeDescriptor = automaton.memoryDescriptors[0] as InputTapeDescriptor
        }

        protected abstract fun getInputOutputPairs(): List<Pair<String, String>>

        @ParameterizedTest
        @MethodSource("getInputOutputPairs")
        fun test(inputOutputPair: Pair<String, String>) =
            assertEquals(inputOutputPair.second, getResult(inputOutputPair.first))

        private fun getResult(input: String): String {
            tapeDescriptor.value = input
            automaton.executor.start()
            automaton.executor.runFor(MAX_RUN_MILLIS)
            return (automaton.executor.acceptedStates.first().memory[1] as OutputTape).value
        }
    }

    @Nested
    inner class ThreeZeroesAndOneOne {
        private lateinit var automaton: Automaton
        private lateinit var tapeDescriptor: InputTapeDescriptor
        private lateinit var registerDescriptor: RegisterDescriptor

        @BeforeEach
        fun init() {
            automaton = TestAutomatons.THREE_ZEROES_AND_ONE_ONE
            tapeDescriptor = automaton.memoryDescriptors[0] as InputTapeDescriptor
            registerDescriptor = automaton.memoryDescriptors[1] as RegisterDescriptor
        }

        @ParameterizedTest
        @CsvSource(
            "1000,,ACCEPTED",
            "0100,,ACCEPTED",
            "0010,,ACCEPTED",
            "0001,,ACCEPTED",
            "0001,,ACCEPTED",
            "00100,,REJECTED",
            "000,,REJECTED",
            "000,1,ACCEPTED"
        )
        fun test(input: String?, initRegister: String?, output: ExecutionStatus) =
            assertEquals(output, getResult(input ?: "", initRegister))

        private fun getResult(input: String, initRegister: String?): ExecutionStatus {
            tapeDescriptor.value = input
            initRegister?.let { registerDescriptor.value = it }
            automaton.executor.start()
            automaton.executor.runFor(MAX_RUN_MILLIS)
            return automaton.executor.status
        }
    }
}
