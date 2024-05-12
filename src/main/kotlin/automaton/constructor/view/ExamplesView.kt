package automaton.constructor.view

import automaton.constructor.controller.FileController
import automaton.constructor.utils.I18N
import javafx.beans.property.StringProperty
import javafx.geometry.Insets
import javafx.scene.control.ListCell
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import tornadofx.*
import kotlin.io.path.toPath

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
        val examplesURL = this@ExamplesView::class.java.getResource("/examples/examples.json")
        if (examplesURL == null) {
            I18N.messages.getString(
                "ExamplesFragment.UnableToFindResource") + " /examples/examples.json"
        } else {
            val deserializedExamples = Json.decodeFromString<List<Example>>(
                examplesURL.readText()
            )
            deserializedExamples.forEach { examples.add(it) }
        }
    }
    override val root = hbox(5) {
        val examplesListView = listview(examples)
        val description = Text().apply { text = I18N.messages.getString("ExamplesFragment.Choose") }
        val image = ImageView()
        val descriptionVBox = VBox(description, image)
        val automatonName = "".toProperty()
        minWidth = 800.0
        padding = Insets(5.0, 5.0, 5.0, 5.0)

        add(descriptionVBox)

        examplesListView.setCellFactory { ExampleCell(automatonName) }

        examplesListView.selectionModel.selectedItemProperty().addListener(ChangeListener { _, _, newValue ->
            description.text = I18N.automatonExamples.getString("ExamplesFragment.${newValue.name}Description")
            val imageURL = this@ExamplesView::class.java.getResource("/examples/images/${newValue.name}.png")
            if (imageURL == null) {
                error(I18N.messages.getString(
                    "ExamplesFragment.UnableToFindResource") + " /examples/images/${newValue.name}.png")
            } else {
                image.image = Image(imageURL.toExternalForm(), true)
            }
        })

        automatonName.addListener(ChangeListener { _, _, newValue ->
            val automatonURL = this@ExamplesView::class.java.getResource("/examples/automatons/${newValue}.atmtn")
            if (automatonURL == null) {
                error(I18N.messages.getString("TestsController.UnableToOpen"))
            } else {
                fileController.open(automatonURL.toURI().toPath().toFile())
                this@ExamplesView.close()
            }
        })
    }
}
