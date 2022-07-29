package automaton.constructor.model.data

import automaton.constructor.model.memory.MemoryUnitDescriptor
import automaton.constructor.model.memory.RegisterDescriptor
import automaton.constructor.model.memory.StackDescriptor
import automaton.constructor.model.memory.tape.OutputTapeDescriptor
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
@IgnorableByCoverage
object InputTapeDescriptorData : MemoryUnitDescriptorData {
    override fun createDescriptor() = InputTapeDescriptor()
}

/**
 * The data of a [multi-track tape descriptor][MultiTrackTapeDescriptor] with the [trackCount] number of tracks.
 */
@Serializable
@SerialName("multi-track-tape")
@IgnorableByCoverage
data class MultiTrackTapeDescriptorData(val trackCount: Int) : MemoryUnitDescriptorData {
    override fun createDescriptor() = MultiTrackTapeDescriptor(trackCount)
}

/**
 * The data of a [stack descriptor][StackDescriptor] with the [acceptsByEmptyStack] status.
 */
@Serializable
@SerialName("stack")
@IgnorableByCoverage
data class StackDescriptorData(val acceptsByEmptyStack: Boolean) : MemoryUnitDescriptorData {
    override fun createDescriptor() = StackDescriptor(acceptsByEmptyStack)
}

/**
 * The data of a [register descriptor][RegisterDescriptor].
 */
@Serializable
@SerialName("register")
@IgnorableByCoverage
object RegisterDescriptorData : MemoryUnitDescriptorData {
    override fun createDescriptor() = RegisterDescriptor()
}

/**
 * The data of a [Mealy/Moore output tape descriptor][OutputTapeDescriptor].
 */
@Serializable
@SerialName("output-tape")
@IgnorableByCoverage
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
