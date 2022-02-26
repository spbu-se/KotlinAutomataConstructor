package automaton.constructor.model.serializers

import automaton.constructor.model.Automaton
import javafx.stage.FileChooser.ExtensionFilter
import java.io.File

interface FileAutomatonSerializer {
    val extensionFilter: ExtensionFilter
    fun serialize(file: File, automaton: Automaton)
    fun deserialize(file: File): Automaton
}

fun fileAutomatonSerializers() = listOf(JsonAutomatonSerializer)
