package automaton.constructor.model.data

import automaton.constructor.model.memory.MemoryUnitDescriptor
import automaton.constructor.model.memory.RegisterDescriptor
import automaton.constructor.model.memory.StackDescriptor
import automaton.constructor.model.memory.output.MealyMooreOutputTapeDescriptor
import automaton.constructor.model.memory.tape.InputTapeDescriptor
import automaton.constructor.model.memory.tape.MultiTrackTapeDescriptor
import automaton.constructor.utils.IgnorableByCoverage
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

/**
 * The data of a [memory unit descriptor][MemoryUnitDescriptor].
 */
@Serializable
@IgnorableByCoverage
abstract class MemoryUnitDescriptorData {
    /**
     * Creates an appropriate [memory unit descriptor][MemoryUnitDescriptor] using this data.
     */
    abstract fun createDescriptor(): MemoryUnitDescriptor
}


/**
 * The data of an [input tape descriptor][InputTapeDescriptor].
 */
@Serializable
@SerialName("Input tape")
@IgnorableByCoverage
object InputTapeDescriptorData : MemoryUnitDescriptorData() {
    override fun createDescriptor() = InputTapeDescriptor()
}

/**
 * The data of a [multi-track tape descriptor][MultiTrackTapeDescriptor] with the [trackCount] number of tracks.
 */
@Serializable
@SerialName("Multi-track tape")
@IgnorableByCoverage
data class MultiTrackTapeDescriptorData(val trackCount: Int) : MemoryUnitDescriptorData() {
    override fun createDescriptor() = MultiTrackTapeDescriptor(trackCount)
}

/**
 * The data of a [stack descriptor][StackDescriptor] with the [acceptsByEmptyStack] status.
 */
@Serializable
@SerialName("Stack")
@IgnorableByCoverage
data class StackDescriptorData(val acceptsByEmptyStack: Boolean) : MemoryUnitDescriptorData() {
    override fun createDescriptor() = StackDescriptor(acceptsByEmptyStack)
}

/**
 * The data of a [register descriptor][RegisterDescriptor].
 */
@Serializable
@SerialName("Register")
@IgnorableByCoverage
object RegisterDescriptorData : MemoryUnitDescriptorData() {
    override fun createDescriptor() = RegisterDescriptor()
}

/**
 * The data of a [Mealy/Moore output tape descriptor][MealyMooreOutputTapeDescriptor].
 */
@Serializable
@SerialName("Mealy/Moore output tape")
@IgnorableByCoverage
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
