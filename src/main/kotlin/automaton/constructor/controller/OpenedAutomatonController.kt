package automaton.constructor.controller

import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.factory.getAllAutomatonFactories
import automaton.constructor.model.serializers.AutomatonSerializer
import automaton.constructor.model.serializers.automatonSerializers
import automaton.constructor.model.toAutomaton
import automaton.constructor.model.toData
import automaton.constructor.utils.I18N.labels
import automaton.constructor.utils.*
import javafx.concurrent.Task
import javafx.geometry.Pos
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import tornadofx.*
import java.io.File
import java.text.MessageFormat

class OpenedAutomatonController(val view: View) {
    private val openedFileProperty = objectProperty<File?>(null)
    private var openedFile: File? by openedFileProperty

    val openedAutomatonProperty = getAllAutomatonFactories().first().createAutomaton().toProperty()
    var openedAutomaton: Automaton by openedAutomatonProperty

    private val nameBinding = openedFileProperty.nonNullObjectBinding(openedAutomatonProperty) {
        it?.toString() ?: MessageFormat.format(labels.getString("OpenedAutomatonController.UntitledAutomaton"),
            openedAutomaton.typeName)
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
        if (!suggestSavingChanges()) return
        view.dialog(labels.getString("OpenedAutomatonController.SelectAutomatonType")) {
            clear()
            stage.x = 100.0
            stage.y = 100.0
            stage.isResizable = false
            vbox(10.0) {
                label(labels.getString("OpenedAutomatonController.NewAutomaton"))
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
                    button(labels.getString("OpenedAutomatonController.OK")) {
                        enableWhen(listview.selectionModel.selectedItemProperty().isNotNull)
                        action {
                            listview.selectedItem?.let {
                                openedAutomaton = it.createAutomaton()
                                openedFile = null
                            }
                            close()
                        }
                    }
                    button(labels.getString("OpenedAutomatonController.Cancel")) { action { close() } }
                }
            }
            scene.widthProperty().onChange { stage.sizeToScene() }
            scene.heightProperty().onChange { stage.sizeToScene() }
        }
    }

    fun onOpen() {
        if (!suggestSavingChanges()) return
        val file = chooseFile(labels.getString("MainView.File.Open"), FileChooserMode.Single) ?: return
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
        saveAs(chooseFile(labels.getString("MainView.File.SaveAs"), FileChooserMode.Save)
            ?: return false)
        return true
    }

    private fun saveAs(file: File) {
        findFileAutomatonSerializer(file).saveAsync(file)
    }

    private fun chooseFile(title: String, mode: FileChooserMode): File? =
        chooseFile(
            title = title,
            filters = automatonSerializers().map { it.extensionFilter }.toTypedArray(),
            initialDirectory = openedFile?.parentFile ?: defaultDirectory(),
            mode = mode,
            owner = view.currentWindow,
            initialFileName = openedFile?.name ?: name
        ).firstOrNull()

    private fun defaultDirectory() = runCatching {
        File("${System.getProperty("user.home")}/Documents/automaton-constructor").takeIf { it.isDirectory || it.mkdirs() }
    }.getOrNull()

    private fun findFileAutomatonSerializer(file: File) =
        automatonSerializers().find { serializer ->
            serializer.extensionFilter.extensions.any { file.path.endsWith(it.drop(1)) }
        } ?: throw IllegalArgumentException(MessageFormat.format(labels.getString("OpenedAutomatonController.FindFileAutomatonSerializer.IllegalArgumentException"),
            file.extension, file))

    private fun AutomatonSerializer.saveAsync(file: File): Task<Unit> {
        val automatonData = openedAutomaton.toData()
        openedAutomaton.undoRedoManager.wasModified = false
        return view.runAsyncWithDialog(MessageFormat.format(labels.getString("OpenedAutomatonController.SavingAutomaton"), file),
            daemon = false) {
            serialize(file, automatonData)
        } addOnSuccess {
            openedFile = file
        } addOnFail {
            openedAutomaton.undoRedoManager.wasModified = true
            throw RuntimeException(MessageFormat.format(labels.getString("OpenedAutomatonController.SaveAsync.RuntimeException"),
                file), it)
        } addOnCancel {
            openedAutomaton.undoRedoManager.wasModified = true
        }
    }

    private fun AutomatonSerializer.loadAsync(file: File): Task<Automaton> =
        view.runAsyncWithDialog(MessageFormat.format(labels.getString("OpenedAutomatonController.LoadingAutomaton"), file),
            daemon = true) {
            deserialize(file).toAutomaton()
        } addOnSuccess {
            openedAutomaton = it
            openedFile = file
        } addOnFail {
            throw RuntimeException(MessageFormat.format(labels.getString("OpenedAutomatonController.LoadAsync.RuntimeException"),file), it)
        }

    /**
     * @return false if CANCEL is pressed
     */
    private fun suggestSavingChanges(): Boolean {
        if (!openedAutomaton.undoRedoManager.wasModified) return true
        val result = alert(
            Alert.AlertType.CONFIRMATION,
            MessageFormat.format(labels.getString("OpenedAutomatonController.SuggestSavingChanges"), name),
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
