package automaton.constructor.model.serializers

import automaton.constructor.model.data.AutomatonData
import javafx.stage.FileChooser.ExtensionFilter
import java.io.File

interface AutomatonSerializer {
    val extensionFilter: ExtensionFilter
    fun serialize(file: File, automatonData: AutomatonData)
    fun deserialize(file: File): AutomatonData
}

fun automatonSerializers() = listOf(JsonAutomatonSerializer)
