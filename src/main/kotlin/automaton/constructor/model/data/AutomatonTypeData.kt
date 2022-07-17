package automaton.constructor.model.data

import automaton.constructor.model.automaton.*
import automaton.constructor.utils.IgnorableByCoverage
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

@IgnorableByCoverage
@Serializable
abstract class AutomatonTypeData {
    abstract fun createEmptyAutomaton(): Automaton
}


@IgnorableByCoverage
@Serializable
@SerialName(FiniteAutomaton.NAME)
data class FiniteAutomatonData(
    val inputTape: InputTapeDescriptorData
) : AutomatonTypeData() {
    override fun createEmptyAutomaton() = FiniteAutomaton(
        inputTape = inputTape.createDescriptor()
    )
}

@IgnorableByCoverage
@Serializable
@SerialName(PushdownAutomaton.NAME)
data class PushdownAutomatonData(
    val inputTape: InputTapeDescriptorData,
    val stacks: List<StackDescriptorData>
) : AutomatonTypeData() {
    override fun createEmptyAutomaton() = PushdownAutomaton(
        inputTape = inputTape.createDescriptor(),
        stacks = stacks.map { it.createDescriptor() }
    )
}

@IgnorableByCoverage
@Serializable
@SerialName(RegisterAutomaton.NAME)
data class RegisterAutomatonData(
    val inputTape: InputTapeDescriptorData,
    val registers: List<RegisterDescriptorData>
) : AutomatonTypeData() {
    override fun createEmptyAutomaton() = RegisterAutomaton(
        inputTape = inputTape.createDescriptor(),
        registers = registers.map { it.createDescriptor() }
    )
}

@IgnorableByCoverage
@Serializable
@SerialName(MealyMooreMachine.NAME)
data class MealyMooreMachineData(
    val inputTape: InputTapeDescriptorData,
    val mealyMooreOutputTape: MealyMooreOutputTapeDescriptorData
) : AutomatonTypeData() {
    override fun createEmptyAutomaton() = MealyMooreMachine(
        inputTape = inputTape.createDescriptor(),
        mealyMooreOutputTape = mealyMooreOutputTape.createDescriptor()
    )
}

@IgnorableByCoverage
@Serializable
@SerialName(TuringMachine.NAME)
data class TuringMachineData(
    val tape: MultiTrackTapeDescriptorData
) : AutomatonTypeData() {
    override fun createEmptyAutomaton() = TuringMachine(
        tape = tape.createDescriptor()
    )
}

@IgnorableByCoverage
@Serializable
@SerialName(MultiTrackTuringMachine.NAME)
data class MultiTrackTuringMachineData(
    val tracks: MultiTrackTapeDescriptorData
) : AutomatonTypeData() {
    override fun createEmptyAutomaton() = MultiTrackTuringMachine(
        tracks = tracks.createDescriptor()
    )
}

@IgnorableByCoverage
@Serializable
@SerialName(MultiTapeTuringMachine.NAME)
data class MultiTapeTuringMachineData(
    val tapes: List<MultiTrackTapeDescriptorData>
) : AutomatonTypeData() {
    override fun createEmptyAutomaton() = MultiTapeTuringMachine(
        tapes = tapes.map { it.createDescriptor() }
    )
}

@IgnorableByCoverage
@Serializable
@SerialName(TuringMachineWithRegisters.NAME)
data class TuringMachineWithRegistersData(
    val tape: MultiTrackTapeDescriptorData,
    val registers: List<RegisterDescriptorData>
) : AutomatonTypeData() {
    override fun createEmptyAutomaton() = TuringMachineWithRegisters(
        tape = tape.createDescriptor(),
        registers = registers.map { it.createDescriptor() }
    )
}

@IgnorableByCoverage
@Serializable
@SerialName(CustomAutomaton.NAME)
data class CustomAutomatonData(
    val memoryUnitDescriptors: List<MemoryUnitDescriptorData>
) : AutomatonTypeData() {
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
