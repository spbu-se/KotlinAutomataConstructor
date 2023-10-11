package automaton.constructor.view

import automaton.constructor.controller.FileController
import javafx.beans.property.IntegerProperty
import javafx.beans.property.StringProperty
import javafx.scene.control.ListCell
import javafx.scene.text.Text
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import tornadofx.*
import java.io.File
import java.nio.file.Files
import kotlin.io.path.Path

@Serializable
data class Example(val name: String, val description: String)

class ExampleCell(
    private val counter: IntegerProperty,
    private val automatonName: StringProperty
): ListCell<Example?>() {
    init {
        this.setOnMouseClicked {
            if (it.clickCount == 2 && this.item != null)
                automatonName.set(this.item!!.name)
        }
    }
    override fun updateItem(item: Example?, empty: Boolean) {
        super.updateItem(item, empty)
        graphic = if (item != null) {
            counter.set(counter.value + 1) // this whole counter thing obviously needs to be remade
            Text().apply { text = item.name }
        } else
            null
    }
}

class ExamplesFragment: Fragment() {
    val fileController: FileController by param()
    private val examples = mutableListOf<Example>().asObservable()
    init {
        val examplesPath = Path("${System.getProperty("user.dir")}/src/main/resources/examples/examples.json")
        val deserializedExamples = Json.decodeFromString<List<Example>>(examplesPath.toFile().readText())
        deserializedExamples.forEach { examples.add(it) }
    }
    override val root = hbox {
        val examplesListView = listview(examples)
        val description = Text().apply { text = "Choose an example" }
        val counter = 0.toProperty()
        val automatonName = "".toProperty()
        examplesListView.setCellFactory { ExampleCell(counter, automatonName) }
        add(description)
        counter.addListener(ChangeListener { _, _, _ ->
            val selectedCellIndex = examplesListView.selectionModel.selectedIndex
            if (selectedCellIndex != -1) {
                description.text = examples[selectedCellIndex].description
            }
        })
        automatonName.addListener(ChangeListener { _, _, newValue ->
            val automatonsPath = Path("${System.getProperty("user.dir")}/src/main/resources/examples/automatons")
            Files.walk(automatonsPath).forEach {
                if (it.toFile().name == "$newValue.atmtn") {
                    fileController.open(it.toFile())
                    this@ExamplesFragment.close()
                }
            }
        })
    }
}
