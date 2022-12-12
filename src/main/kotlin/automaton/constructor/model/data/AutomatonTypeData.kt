package automaton.constructor.model.data

import automaton.constructor.model.automaton.*
import automaton.constructor.utils.MostlyGeneratedOrInline
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

/**
 * The type data of an [automaton][Automaton].
 */
interface AutomatonTypeData {
    /**
     * Creates an appropriate empty [automaton][Automaton] using this type data.
     */
    fun createEmptyAutomaton(): Automaton

    companion object
}


/**
 * The data of a [finite automaton][FiniteAutomaton] with an [input tape][inputTape].
 */
@MostlyGeneratedOrInline
@Serializable
@SerialName("finite-automaton")
data class FiniteAutomatonData(
    val inputTape: InputTapeDescriptorData
) : AutomatonTypeData {
    override fun createEmptyAutomaton() = FiniteAutomaton(
        inputTape = inputTape.createDescriptor()
    )
}

/**
 * The data of a [pushdown automaton][PushdownAutomaton] with an [input tape][inputTape] and several [stacks].
 */
@MostlyGeneratedOrInline
@Serializable
@SerialName("pushdown-automaton")
data class PushdownAutomatonData(
    val inputTape: InputTapeDescriptorData,
    val stacks: List<StackDescriptorData>
) : AutomatonTypeData {
    override fun createEmptyAutomaton() = PushdownAutomaton(
        inputTape = inputTape.createDescriptor(),
        stacks = stacks.map { it.createDescriptor() }
    )
}

/**
 * The data of a [register automaton][RegisterAutomaton] with an [input tape][inputTape] and several [registers].
 */
@MostlyGeneratedOrInline
@Serializable
@SerialName("register-automaton")
data class RegisterAutomatonData(
    val inputTape: InputTapeDescriptorData,
    val registers: List<RegisterDescriptorData>
) : AutomatonTypeData {
    override fun createEmptyAutomaton() = RegisterAutomaton(
        inputTape = inputTape.createDescriptor(),
        registers = registers.map { it.createDescriptor() }
    )
}

/**
 * The data of a [Mealy/Moore machine][MealyMooreMachine] with an [input tape][inputTape] and a [Mealy/Moore output tape][outputTape].
 */
@MostlyGeneratedOrInline
@Serializable
@SerialName("mealy-moore-machine")
data class MealyMooreMachineData(
    val inputTape: InputTapeDescriptorData,
    val outputTape: OutputTapeDescriptorData
) : AutomatonTypeData {
    override fun createEmptyAutomaton() = MealyMooreMachine(
        inputTape = inputTape.createDescriptor(),
        outputTape = outputTape.createDescriptor()
    )
}

/**
 * The data of a [Turing machine][TuringMachine] with a [tape].
 */
@MostlyGeneratedOrInline
@Serializable
@SerialName("turing-machine")
data class TuringMachineData(
    val tape: MultiTrackTapeDescriptorData
) : AutomatonTypeData {
    override fun createEmptyAutomaton() = TuringMachine(
        tape = tape.createDescriptor()
    )
}

/**
 * The data of a [multi-track Turing machine][MultiTrackTuringMachine] with several [tracks].
 */
@MostlyGeneratedOrInline
@Serializable
@SerialName("multi-track-turing-machine")
data class MultiTrackTuringMachineData(
    val tracks: MultiTrackTapeDescriptorData
) : AutomatonTypeData {
    override fun createEmptyAutomaton() = MultiTrackTuringMachine(
        tracks = tracks.createDescriptor()
    )
}

/**
 * The data of a [multi-tape Turing machine][MultiTapeTuringMachine] with several [tapes].
 */
@MostlyGeneratedOrInline
@Serializable
@SerialName("multi-tape-turing-machine")
data class MultiTapeTuringMachineData(
    val tapes: List<MultiTrackTapeDescriptorData>
) : AutomatonTypeData {
    override fun createEmptyAutomaton() = MultiTapeTuringMachine(
        tapes = tapes.map { it.createDescriptor() }
    )
}

/**
 * The data of a [Turing machine with registers][TuringMachineWithRegisters] with a [tape] and several [registers].
 */
@MostlyGeneratedOrInline
@Serializable
@SerialName("turing-machine-with-registers")
data class TuringMachineWithRegistersData(
    val tape: MultiTrackTapeDescriptorData,
    val registers: List<RegisterDescriptorData>
) : AutomatonTypeData {
    override fun createEmptyAutomaton() = TuringMachineWithRegisters(
        tape = tape.createDescriptor(),
        registers = registers.map { it.createDescriptor() }
    )
}

/**
 * The data of a [custom automaton][CustomAutomaton] with a custom list of [memory descriptors][memoryUnitDescriptors].
 */
@MostlyGeneratedOrInline
@Serializable
@SerialName("custom-automaton")
data class CustomAutomatonData(
    val memoryUnitDescriptors: List<MemoryUnitDescriptorData>
) : AutomatonTypeData {
    override fun createEmptyAutomaton() = CustomAutomaton(
        memoryDescriptors = memoryUnitDescriptors.map { it.createDescriptor() }
    )
}


val AutomatonTypeData.Companion.serializersModule
    get() = SerializersModule {
        polymorphic(AutomatonTypeData::class) {
            subclass(FiniteAutomatonData::class)
            subclass(PushdownAutomatonData::class)
            subclass(RegisterAutomatonData::class)
            subclass(MealyMooreMachineData::class)
            subclass(TuringMachineData::class)
            subclass(MultiTrackTuringMachineData::class)
            subclass(MultiTapeTuringMachineData::class)
            subclass(TuringMachineWithRegistersData::class)
            subclass(CustomAutomatonData::class)
        }
    }
