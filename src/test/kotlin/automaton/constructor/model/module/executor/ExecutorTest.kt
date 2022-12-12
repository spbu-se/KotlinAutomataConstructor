package automaton.constructor.model.module.executor

import automaton.constructor.model.TestAutomatons
import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.automaton.TuringMachine
import automaton.constructor.model.memory.RegisterDescriptor
import automaton.constructor.model.memory.tape.InputTapeDescriptor
import automaton.constructor.model.memory.tape.MultiTrackTape
import automaton.constructor.model.memory.tape.MultiTrackTapeDescriptor
import automaton.constructor.model.memory.tape.OutputTape
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
            val track = (automaton.executor.acceptedExeStates.first().memory[0] as MultiTrackTape).tracks[0]
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
            assertEquals(output, getResult(input ?: "", StepByStateStrategy))

        @ParameterizedTest
        @CsvSource(",ACCEPTED", "0,REJECTED")
        fun `test step by closure`(input: String?, output: ExecutionStatus) =
            assertEquals(output, getResult(input ?: "", StepByClosureStrategy))

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
            assertEquals(setOf(root), automaton.executor.exeStates)
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
            return (automaton.executor.acceptedExeStates.first().memory[1] as OutputTape).value
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

    abstract class TuringMachineTest(val steppingStrategy: SteppingStrategy) {
        private lateinit var turingMachine: TuringMachine

        @BeforeEach
        fun init() {
            turingMachine = getTuringMachine()
        }

        abstract fun getTuringMachine(): TuringMachine

        abstract fun getTestData(): List<Array<out Any>>

        open fun checkStateCount() = true

        @ParameterizedTest
        @MethodSource("getTestData")
        fun test(input: String, output: String, forEveryStateCollapseAndRerun: Boolean) =
            assertEquals(output, getResult(input, forEveryStateCollapseAndRerun))

        private fun getResult(input: String, forEveryStateCollapseAndRerun: Boolean): String {
            turingMachine.tape.valueProperties[0].value = input
            turingMachine.executor.start()
            fun evalResult(): String {
                turingMachine.executor.runFor(strategy = steppingStrategy)
                val track = (turingMachine.executor.acceptedExeStates.first().memory[0] as MultiTrackTape).tracks[0]
                return track.processed + track.current + track.unprocessed
            }

            val controlResult = evalResult()
            if (forEveryStateCollapseAndRerun) {
                fun Executor.allExeStates(): List<ExecutionState> = exeStates.flatMap { exeState ->
                    when (exeState) {
                        is SimpleExecutionState -> listOf(exeState)
                        is SuperExecutionState -> exeState.subExecutor.allExeStates() + exeState
                    }
                }

                val finalExeStatesCount = turingMachine.executor.allExeStates().size
                repeat(finalExeStatesCount) { i ->
                    val allExeStatesAtTheMoment = turingMachine.executor.allExeStates()
                    if (checkStateCount()) assertEquals(allExeStatesAtTheMoment.size, finalExeStatesCount)
                    else if (allExeStatesAtTheMoment.size < i) return@repeat
                    allExeStatesAtTheMoment[i].collapse()
                    assertEquals(evalResult(), controlResult, "$i")
                }
            }
            return controlResult
        }
    }

    abstract class BinaryAddition(steppingStrategy: SteppingStrategy) : TuringMachineTest(steppingStrategy) {
        override fun getTuringMachine() = TestAutomatons.BINARY_ADDITION

        override fun getTestData() = listOf(
            arrayOf("11001+1011", "100100", false),
            arrayOf("0+11", "11", false),
            arrayOf("10+0", "10", false),
            arrayOf("0+0", "0", false),
            arrayOf("0+11", "11", true),
            arrayOf("10+0", "10", true),
            arrayOf("0+0", "0", true)
        )
    }

    @Nested
    inner class BinaryAdditionStepByClosure : BinaryAddition(StepByClosureStrategy)

    @Nested
    inner class BinaryAdditionStepInto : BinaryAddition(StepIntoStrategy)

    @Nested
    inner class BinaryAdditionStepOver : BinaryAddition(StepOverStrategy)

    abstract class ElevenRecogniserToGenerator(steppingStrategy: SteppingStrategy) : TuringMachineTest(steppingStrategy) {
        override fun getTuringMachine() = TestAutomatons.ELEVEN_RECOGNISER_TO_GENERATOR

        override fun getTestData() = listOf(
            arrayOf("", "11", false),
            arrayOf("", "11", true)
        )

        override fun checkStateCount() = false
    }

    @Nested
    inner class ElevenRecogniserToGeneratorStepByClosure : ElevenRecogniserToGenerator(StepByClosureStrategy)

    @Nested
    inner class ElevenRecogniserToGeneratorStepInto : ElevenRecogniserToGenerator(StepIntoStrategy)
}
