package automaton.constructor.model.serializers

import automaton.constructor.model.TestAutomatons
import automaton.constructor.model.data.AutomatonData
import automaton.constructor.model.data.getData
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.io.File
import java.util.stream.Stream
import kotlin.test.assertEquals

class AutomatonSerializerTest {
    @ParameterizedTest
    @MethodSource("composition of serialization and deserialization should leave automaton data unchanged source")
    fun `composition of serialization and deserialization should leave automaton data unchanged`(
        serializer: AutomatonSerializer,
        automatonData: AutomatonData
    ) {
        val tempFile = File.createTempFile("temp-automaton-serializer-test", "")
        serializer.serialize(tempFile, automatonData)
        assertEquals(
            automatonData,
            serializer.deserialize(tempFile)
        )
        tempFile.delete()
    }

    private fun `composition of serialization and deserialization should leave automaton data unchanged source`(): Stream<Arguments> =
        automatonSerializers().stream().flatMap { serializer ->
            TestAutomatons.allAutomataStream().map { automaton ->
                Arguments.of(serializer, automaton.getData())
            }
        }
}