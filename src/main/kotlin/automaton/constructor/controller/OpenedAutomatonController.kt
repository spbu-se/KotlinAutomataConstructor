package automaton.constructor.controller

import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.data.createAutomaton
import automaton.constructor.model.data.getData
import automaton.constructor.model.factory.getAllAutomatonFactories
import automaton.constructor.model.serializers.AutomatonSerializer
import automaton.constructor.model.serializers.automatonSerializers
import automaton.constructor.utils.I18N.messages
import automaton.constructor.utils.addOnCancel
import automaton.constructor.utils.addOnFail
import automaton.constructor.utils.addOnSuccess
import automaton.constructor.utils.nonNullObjectBinding
import automaton.constructor.utils.runAsyncWithDialog
import javafx.beans.binding.Binding
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

    private val nameBinding: Binding<String> = openedFileProperty.nonNullObjectBinding(openedAutomatonProperty) {
        it?.toString() ?: MessageFormat.format(
            openedAutomaton.untitledDisplayName,
            openedAutomaton.typeDisplayName
        )
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
        view.dialog(messages.getString("OpenedAutomatonController.NewAutomaton")) {
            clear()
            stage.x = 100.0
            stage.y = 100.0
            stage.isResizable = false
            vbox(10.0) {
                label(messages.getString("OpenedAutomatonController.SelectAutomatonType"))
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
                    button(messages.getString("OpenedAutomatonController.OK")) {
                        enableWhen(listview.selectionModel.selectedItemProperty().isNotNull)
                        action {
                            listview.selectedItem?.let {
                                openedAutomaton = it.createAutomaton()
                                openedFile = null
                            }
                            close()
                        }
                    }
                    button(messages.getString("OpenedAutomatonController.Cancel")) { action { close() } }
                }
            }
            scene.widthProperty().onChange { stage.sizeToScene() }
            scene.heightProperty().onChange { stage.sizeToScene() }
        }
    }

    fun onOpen() {
        if (!suggestSavingChanges()) return
        val file = chooseFile(messages.getString("MainView.File.Open"), FileChooserMode.Single) ?: return
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
        saveAs(
            chooseFile(messages.getString("MainView.File.SaveAs"), FileChooserMode.Save)
                ?: return false
        )
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
        } ?: throw IllegalArgumentException(
            MessageFormat.format(
                messages.getString("OpenedAutomatonController.UnknownFileExtension"),
                file.extension, file
            )
        )

    private fun AutomatonSerializer.saveAsync(file: File): Task<Unit> {
        val automatonData = openedAutomaton.getData()
        openedAutomaton.undoRedoManager.wasModified = false
        return view.runAsyncWithDialog(
            MessageFormat.format(messages.getString("OpenedAutomatonController.SavingAutomaton"), file),
            daemon = false
        ) {
            serialize(file, automatonData)
        } addOnSuccess {
            openedFile = file
        } addOnFail {
            openedAutomaton.undoRedoManager.wasModified = true
            throw RuntimeException(
                MessageFormat.format(
                    messages.getString("OpenedAutomatonController.UnableToSaveAutomaton"),
                    file
                ), it
            )
        } addOnCancel {
            openedAutomaton.undoRedoManager.wasModified = true
        }
    }

    private fun AutomatonSerializer.loadAsync(file: File): Task<Automaton> =
        view.runAsyncWithDialog(
            MessageFormat.format(messages.getString("OpenedAutomatonController.LoadingAutomaton"), file),
            daemon = true
        ) {
            deserialize(file).createAutomaton()
        } addOnSuccess {
            openedAutomaton = it
            openedFile = file
        } addOnFail {
            throw RuntimeException(
                MessageFormat.format(
                    messages.getString("OpenedAutomatonController.UnableToLoadAutomaton"),
                    file
                ), it
            )
        }

    /**
     * @return false if CANCEL is pressed
     */
    private fun suggestSavingChanges(): Boolean {
        if (!openedAutomaton.undoRedoManager.wasModified) return true
        val result = alert(
            Alert.AlertType.CONFIRMATION,
            MessageFormat.format(messages.getString("OpenedAutomatonController.SuggestSavingChanges"), name),
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
