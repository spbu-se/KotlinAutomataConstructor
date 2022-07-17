package automaton.constructor.model.data

import automaton.constructor.model.memory.MemoryUnitDescriptor
import automaton.constructor.model.memory.RegisterDescriptor
import automaton.constructor.model.memory.StackDescriptor
import automaton.constructor.model.memory.output.MealyMooreOutputTapeDescriptor
import automaton.constructor.model.memory.tape.InputTapeDescriptor
import automaton.constructor.model.memory.tape.MultiTrackTapeDescriptor
import automaton.constructor.utils.MostlyGeneratedOrInline
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

@Serializable
@MostlyGeneratedOrInline
abstract class MemoryUnitDescriptorData {
    abstract fun createDescriptor(): MemoryUnitDescriptor
}


@Serializable
@SerialName("Input tape")
@MostlyGeneratedOrInline
object InputTapeDescriptorData : MemoryUnitDescriptorData() {
    override fun createDescriptor() = InputTapeDescriptor()
}

@Serializable
@SerialName("Multi-track tape")
@MostlyGeneratedOrInline
data class MultiTrackTapeDescriptorData(val trackCount: Int) : MemoryUnitDescriptorData() {
    override fun createDescriptor() = MultiTrackTapeDescriptor(trackCount)
}

@Serializable
@SerialName("Stack")
@MostlyGeneratedOrInline
data class StackDescriptorData(val acceptsByEmptyStack: Boolean) : MemoryUnitDescriptorData() {
    override fun createDescriptor() = StackDescriptor(acceptsByEmptyStack)
}

@Serializable
@SerialName("Register")
@MostlyGeneratedOrInline
object RegisterDescriptorData : MemoryUnitDescriptorData() {
    override fun createDescriptor() = RegisterDescriptor()
}

@Serializable
@SerialName("Mealy/Moore output tape")
@MostlyGeneratedOrInline
object MealyMooreOutputTapeDescriptorData : MemoryUnitDescriptorData() {
    override fun createDescriptor() = MealyMooreOutputTapeDescriptor()
}


val MemoryUnitDescriptorData.Companion.serializersModule
    get() = SerializersModule {
        polymorphic(MemoryUnitDescriptorData::class) {
            subclass(InputTapeDescriptorData::class)
            subclass(MultiTrackTapeDescriptorData::class)
            subclass(StackDescriptorData::class)
            subclass(RegisterDescriptorData::class)
            subclass(MealyMooreOutputTapeDescriptorData::class)
        }
    }
