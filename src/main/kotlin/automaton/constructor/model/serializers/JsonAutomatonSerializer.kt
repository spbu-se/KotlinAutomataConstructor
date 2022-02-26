package automaton.constructor.model.serializers

import automaton.constructor.model.Automaton
import automaton.constructor.model.memory.memoryUnitDescriptorSerializers
import javafx.stage.FileChooser.ExtensionFilter
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

object JsonAutomatonSerializer : FileAutomatonSerializer {
    private val format = Json {
        prettyPrint = true
        serializersModule = memoryUnitDescriptorSerializers
    }

    override val extensionFilter = ExtensionFilter("Automaton constructor file", "*.atmtn", "*.json")

    override fun serialize(file: File, automaton: Automaton) = file.writeText(format.encodeToString(automaton))

    override fun deserialize(file: File): Automaton = format.decodeFromString(file.readText())
}
