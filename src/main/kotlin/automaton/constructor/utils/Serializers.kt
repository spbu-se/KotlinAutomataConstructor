package automaton.constructor.utils

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.serializer

/**
 * [Composite serializer via surrogate](https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/serializers.md#composite-serializer-via-surrogate)
 */
@IgnorableByCoverage
inline fun <T, reified U> surrogateSerializer(
    crossinline toSurrogate: (T) -> (U),
    crossinline fromSurrogate: (U) -> T
) =
    object : KSerializer<T> {
        override val descriptor get() = serializer<U>().descriptor
        override fun serialize(encoder: Encoder, value: T) = serializer<U>().serialize(encoder, toSurrogate(value))
        override fun deserialize(decoder: Decoder): T = fromSurrogate(serializer<U>().deserialize(decoder))
    }

fun <T> noPropertiesSerializer(serialName: String, constructor: () -> T) = object : KSerializer<T> {
    override val descriptor = buildClassSerialDescriptor(serialName)
    override fun serialize(encoder: Encoder, value: T) = encoder.beginStructure(descriptor).endStructure(descriptor)

    override fun deserialize(decoder: Decoder): T {
        decoder.beginStructure(descriptor).endStructure(descriptor)
        return constructor()
    }
}
