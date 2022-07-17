package automaton.constructor.model.serializers

import automaton.constructor.model.data.AutomatonData
import automaton.constructor.model.data.AutomatonTypeData
import automaton.constructor.model.data.MemoryUnitDescriptorData
import automaton.constructor.model.data.serializersModule
import javafx.stage.FileChooser.ExtensionFilter
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import java.io.File

object JsonAutomatonSerializer : AutomatonSerializer {
    private val format = Json {
        prettyPrint = true
        serializersModule = SerializersModule {
            include(AutomatonTypeData.serializersModule)
            include(MemoryUnitDescriptorData.serializersModule)
        }
    }

    override val extensionFilter = ExtensionFilter("Automaton constructor file", "*.atmtn", "*.json")

    override fun serialize(file: File, automatonData: AutomatonData) =
        file.writeText(format.encodeToString(automatonData))

    override fun deserialize(file: File): AutomatonData = format.decodeFromString(file.readText())
}
