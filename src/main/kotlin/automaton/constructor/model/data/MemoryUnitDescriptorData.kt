package automaton.constructor.model.data

import automaton.constructor.model.memory.MemoryUnitDescriptor
import automaton.constructor.model.memory.RegisterDescriptor
import automaton.constructor.model.memory.StackDescriptor
import automaton.constructor.model.memory.tape.InputTapeDescriptor
import automaton.constructor.model.memory.tape.MultiTrackTapeDescriptor
import automaton.constructor.model.memory.tape.OutputTapeDescriptor
import automaton.constructor.utils.MostlyGeneratedOrInline
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

/**
 * The data of a [memory unit descriptor][MemoryUnitDescriptor].
 */
interface MemoryUnitDescriptorData {
    /**
     * Creates an appropriate [memory unit descriptor][MemoryUnitDescriptor] using this data.
     */
    fun createDescriptor(): MemoryUnitDescriptor

    companion object
}


/**
 * The data of an [input tape descriptor][InputTapeDescriptor].
 */
@Serializable
@SerialName("input-tape")
@MostlyGeneratedOrInline
data class InputTapeDescriptorData(val value: String = "") : MemoryUnitDescriptorData {
    override fun createDescriptor() = InputTapeDescriptor(value)
}

/**
 * The data of a [multi-track tape descriptor][MultiTrackTapeDescriptor] with the [trackCount] number of tracks.
 */
@Serializable
@SerialName("multi-track-tape")
@MostlyGeneratedOrInline
data class MultiTrackTapeDescriptorData(
    val trackCount: Int,
    val values: List<String> = listOf()
) : MemoryUnitDescriptorData {
    override fun createDescriptor() = MultiTrackTapeDescriptor(trackCount, values)
}

/**
 * The data of a [stack descriptor][StackDescriptor] with the [acceptsByEmptyStack] status.
 */
@Serializable
@SerialName("stack")
@MostlyGeneratedOrInline
data class StackDescriptorData(val acceptsByEmptyStack: Boolean, val value: String = "z") : MemoryUnitDescriptorData {
    override fun createDescriptor() = StackDescriptor(acceptsByEmptyStack, value)
}

/**
 * The data of a [register descriptor][RegisterDescriptor].
 */
@Serializable
@SerialName("register")
@MostlyGeneratedOrInline
data class RegisterDescriptorData(val value: String = "0") : MemoryUnitDescriptorData {
    override fun createDescriptor() = RegisterDescriptor(value)
}

/**
 * The data of a [Mealy/Moore output tape descriptor][OutputTapeDescriptor].
 */
@Serializable
@SerialName("output-tape")
@MostlyGeneratedOrInline
object OutputTapeDescriptorData : MemoryUnitDescriptorData {
    override fun createDescriptor() = OutputTapeDescriptor()
}


val MemoryUnitDescriptorData.Companion.serializersModule
    get() = SerializersModule {
        polymorphic(MemoryUnitDescriptorData::class) {
            subclass(InputTapeDescriptorData::class)
            subclass(MultiTrackTapeDescriptorData::class)
            subclass(StackDescriptorData::class)
            subclass(RegisterDescriptorData::class)
            subclass(OutputTapeDescriptorData::class)
        }
    }
