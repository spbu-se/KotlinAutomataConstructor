package automaton.constructor.view

import automaton.constructor.controller.FileController
import automaton.constructor.utils.I18N
import javafx.beans.property.IntegerProperty
import javafx.beans.property.StringProperty
import javafx.scene.control.ListCell
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import tornadofx.*
import java.nio.file.Files
import kotlin.io.path.Path

@Serializable
data class Example(val name: String, val description: String)

class ExampleCell(private val automatonName: StringProperty): ListCell<Example?>() {
    init {
        this.setOnMouseClicked {
            if (it.clickCount == 2 && this.item != null)
                automatonName.set(this.item!!.name)
        }
    }
    override fun updateItem(item: Example?, empty: Boolean) {
        super.updateItem(item, empty)
        graphic = if (item != null) {
            Text().apply { text = I18N.automatonExamples.getString("ExamplesFragment.${item.name}") }
        } else
            null
    }
}

class ExamplesView: View() {
    val fileController: FileController by param()
    private val examples = mutableListOf<Example>().asObservable()
    init {
        val examplesPath = Path("${System.getProperty("user.dir")}/src/main/resources/examples/examples.json")
        val deserializedExamples = Json.decodeFromString<List<Example>>(examplesPath.toFile().readText())
        deserializedExamples.forEach { examples.add(it) }
    }
    override val root = hbox {
        val examplesListView = listview(examples)
        val description = Text().apply { text = I18N.messages.getString("ExamplesFragment.Choose") }
        val image = ImageView()
        val descriptionVBox = VBox(description, image)
        val automatonName = "".toProperty()
        minWidth = 800.0

        add(descriptionVBox)

        examplesListView.setCellFactory { ExampleCell(automatonName) }

        examplesListView.selectionModel.selectedItemProperty().addListener(ChangeListener { _, _, newValue ->
            description.text = I18N.automatonExamples.getString("ExamplesFragment.${newValue.name}Description")
            image.image = Image(
                "file:///${System.getProperty("user.dir")}/src/main/resources/examples/images/${newValue.name}.png",
                true)
        })

        automatonName.addListener(ChangeListener { _, _, newValue ->
            val automatonsPath = Path("${System.getProperty("user.dir")}/src/main/resources/examples/automatons")
            Files.walk(automatonsPath).forEach {
                if (it.toFile().name == "$newValue.atmtn") {
                    fileController.open(it.toFile())
                    this@ExamplesView.close()
                }
            }
        })
    }
}
