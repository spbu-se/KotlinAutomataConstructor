package automaton.constructor.controller

import automaton.constructor.model.Automaton
import automaton.constructor.model.factory.getAllAutomatonFactories
import automaton.constructor.model.serializers.FileAutomatonSerializer
import automaton.constructor.model.serializers.fileAutomatonSerializers
import automaton.constructor.utils.addOnFail
import automaton.constructor.utils.addOnSuccess
import automaton.constructor.utils.nonNullObjectBinding
import automaton.constructor.utils.runAsyncWithDialog
import javafx.concurrent.Task
import javafx.geometry.Pos
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import tornadofx.*
import java.io.File

class OpenedAutomatonController(val view: View) {
    private val openedFileProperty = objectProperty<File?>(null)
    private var openedFile: File? by openedFileProperty

    val openedAutomatonProperty = getAllAutomatonFactories().first().createAutomaton().toProperty()
    var openedAutomaton: Automaton by openedAutomatonProperty

    private val nameBinding = openedFileProperty.nonNullObjectBinding(openedAutomatonProperty) {
        it?.toString() ?: "Untitled ${openedAutomaton.typeName}"
    }
    private val name: String by nameBinding

    val openedAutomatonTitleBinding = nameBinding.nonNullObjectBinding(
        openedAutomatonProperty.select { it.undoRedoManager.wasModifiedProperty }
    ) {
        (if (openedAutomaton.undoRedoManager.wasModified) "*" else "") + name
    }
    val openedAutomatonTitle: String by openedAutomatonTitleBinding

    init {
        view.primaryStage.setOnCloseRequest {
            if (!suggestSavingChanges()) it.consume()
        }
    }

    fun onNew() {
        suggestSavingChanges()
        view.dialog("New automaton") {
            clear()
            stage.x = 100.0
            stage.y = 100.0
            stage.isResizable = false
            vbox(10.0) {
                label("Select automaton type")
                val listview = listview(getAllAutomatonFactories().toObservable()) {
                    prefHeight = 200.0
                }
                borderpane {
                    centerProperty().bind(listview.selectionModel.selectedItemProperty().objectBinding {
                        it?.createEditor()
                    })
                    centerProperty().onChange { stage.sizeToScene() }
                }
                hbox(10.0) {
                    alignment = Pos.CENTER_RIGHT
                    button("OK") {
                        enableWhen(listview.selectionModel.selectedItemProperty().isNotNull)
                        action {
                            listview.selectedItem?.let { openedAutomaton = it.createAutomaton() }
                            close()
                        }
                    }
                    button("Cancel") { action { close() } }
                }
            }
        }
    }

    fun onOpen() {
        suggestSavingChanges()
        val file = chooseFile("Open", FileChooserMode.Single) ?: return
        findFileAutomatonSerializer(file).loadAsync(file)
    }

    /**
     * @return false if CANCEL is pressed
     */
    fun onSave() = openedFile?.let {
        saveAs(it)
        true
    } ?: onSaveAs()

    /**
     * @return false if CANCEL is pressed
     */
    fun onSaveAs(): Boolean {
        saveAs(chooseFile("Save As", FileChooserMode.Save) ?: return false)
        return true
    }

    private fun saveAs(file: File) {
        findFileAutomatonSerializer(file).saveAsync(file)
    }

    private fun chooseFile(title: String, mode: FileChooserMode): File? =
        chooseFile(
            title = title,
            filters = fileAutomatonSerializers().map { it.extensionFilter }.toTypedArray(),
            initialDirectory = openedFile?.parentFile ?: defaultDirectory(),
            mode = mode,
            owner = view.currentWindow,
            initialFileName = openedFile?.name ?: name
        ).firstOrNull()

    private fun defaultDirectory() = runCatching {
        File("${System.getProperty("user.home")}/Documents/automaton-constructor").takeIf { it.isDirectory || it.mkdirs() }
    }.getOrNull()

    private fun findFileAutomatonSerializer(file: File) =
        fileAutomatonSerializers().find { serializer ->
            serializer.extensionFilter.extensions.any { file.path.endsWith(it.drop(1)) }
        } ?: throw IllegalArgumentException("Unknown file extension \"${file.extension}\" of $file")

    private fun FileAutomatonSerializer.saveAsync(file: File): Task<Unit> {
        return view.runAsyncWithDialog("Saving automaton to $file", daemon = false) {
            serialize(file, openedAutomaton)
        } addOnSuccess {
            openedFile = file
            openedAutomaton.undoRedoManager.wasModified = false
        } addOnFail {
            throw RuntimeException("Unable to save graph to $file", it)
        }
    }

    private fun FileAutomatonSerializer.loadAsync(file: File): Task<Automaton> =
        view.runAsyncWithDialog("Loading automaton from $file", daemon = true) {
            deserialize(file)
        } addOnSuccess {
            openedAutomaton = it
            openedFile = file
        } addOnFail {
            throw RuntimeException("Unable to load automaton from $file", it)
        }

    /**
     * @return false if CANCEL is pressed
     */
    private fun suggestSavingChanges(): Boolean {
        if (!openedAutomaton.undoRedoManager.wasModified) return true
        val result = alert(
            Alert.AlertType.CONFIRMATION,
            "Do you want to save $name?",
            null,
            ButtonType.YES,
            ButtonType.NO,
            ButtonType.CANCEL,
            owner = view.currentWindow
        ).result
        return if (result == ButtonType.YES) onSave()
        else result != ButtonType.CANCEL
    }
}
