package automaton.constructor.controller

import automaton.constructor.model.action.ActionAvailability
import automaton.constructor.model.action.transition.SimplifyRegexEntirelyTransitionAction
import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.automaton.untitledName
import automaton.constructor.model.data.AutomatonData
import automaton.constructor.model.data.createAutomaton
import automaton.constructor.model.data.getData
import automaton.constructor.model.factory.AutomatonCreationFailedException
import automaton.constructor.model.factory.getAllAutomatonFactories
import automaton.constructor.model.module.hasRegexes
import automaton.constructor.model.serializers.AutomatonSerializer
import automaton.constructor.model.serializers.automatonSerializers
import automaton.constructor.utils.*
import javafx.beans.binding.Binding
import javafx.concurrent.Task
import javafx.geometry.Pos
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import tornadofx.*
import java.io.File
import java.text.MessageFormat

class FileController(openedAutomaton: Automaton, val uiComponent: UIComponent) {
    private val openedFileProperty = objectProperty<File?>(null)
    private var openedFile: File? by openedFileProperty

    val openedAutomatonProperty = openedAutomaton.toProperty().apply {
        addListener(ChangeListener { _, oldAutomaton, newAutomaton ->
            oldAutomaton.nameProperty.unbind()
            newAutomaton.nameProperty.bind(openedFileProperty.nonNullObjectBinding { file ->
                file?.nameWithoutExtension ?: newAutomaton.untitledName
            })
        })
    }
    var openedAutomaton: Automaton by openedAutomatonProperty

    private val nameBinding: Binding<String> =
        openedFileProperty.nonNullObjectBinding(openedAutomatonProperty) { file ->
            file?.toString() ?: this.openedAutomaton.untitledName
        }
    private val name: String by nameBinding

    val openedAutomatonTitleBinding = nameBinding.nonNullObjectBinding(
        openedAutomatonProperty.select { it.undoRedoManager.wasModifiedProperty }
    ) {
        (if (this.openedAutomaton.undoRedoManager.wasModified) "*" else "") + name
    }
    val openedAutomatonTitle: String by openedAutomatonTitleBinding

    fun onNew() {
        if (!suggestSavingChanges()) return
        uiComponent.dialog(I18N.messages.getString("OpenedAutomatonController.NewAutomaton")) {
            clear()
            uiComponent.currentStage?.icons?.let { stage.icons.addAll(it) }
            stage.x = 100.0
            stage.y = 100.0
            stage.isResizable = false
            vbox(10.0) {
                label(I18N.messages.getString("OpenedAutomatonController.SelectAutomatonType"))
                val listview = listview(getAllAutomatonFactories().toObservable()) {
                    prefHeight = 200.0
                    selectionModel.select(0)
                }
                borderpane {
                    centerProperty().bind(listview.selectionModel.selectedItemProperty().objectBinding {
                        it?.createEditor()
                    })
                    centerProperty().onChange { stage.sizeToScene() }
                }
                hbox(10.0) {
                    alignment = Pos.CENTER_RIGHT
                    button(I18N.messages.getString("OpenedAutomatonController.OK")) {
                        enableWhen(listview.selectionModel.selectedItemProperty().isNotNull)
                        action {
                            listview.selectedItem?.let { automatonFactory ->
                                try {
                                    openedAutomaton = automatonFactory.createAutomaton()
                                } catch (e: AutomatonCreationFailedException) {
                                    error(
                                        e.message ?: "Automaton creation failed",
                                        owner = uiComponent.currentWindow,
                                        title = I18N.messages.getString("Dialog.error")
                                    )
                                    return@action
                                }
                                openedFile = null
                                if (openedAutomaton.hasRegexes) {
                                    val result = alert(
                                        Alert.AlertType.CONFIRMATION,
                                        I18N.messages.getString("SimplifyRegexDialog.Header"),
                                        I18N.messages.getString("SimplifyRegexDialog.Hint"),
                                        ButtonType(I18N.messages.getString("SimplifyRegexDialog.ConvertEntirely"), ButtonType.YES.buttonData),
                                        ButtonType(I18N.messages.getString("SimplifyRegexDialog.ConvertStepByStep"), ButtonType.NO.buttonData),
                                        owner = uiComponent.currentWindow,
                                        title = I18N.messages.getString("Dialog.confirmation")
                                    ).result
                                    if (result.buttonData == ButtonType.YES.buttonData) {
                                        val simplifyRegexAction = openedAutomaton.transitionActions.first { it is SimplifyRegexEntirelyTransitionAction }
                                        openedAutomaton.transitions.toList().forEach {
                                            if (simplifyRegexAction.getAvailabilityFor(it) == ActionAvailability.AVAILABLE)
                                                simplifyRegexAction.performOn(it)
                                        }
                                    }
                                }
                            }
                            close()
                        }
                    }
                    button(I18N.messages.getString("OpenedAutomatonController.Cancel")) { action { close() } }
                }
            }
            scene.widthProperty().onChange { stage.sizeToScene() }
            scene.heightProperty().onChange { stage.sizeToScene() }
        }
    }

    fun onOpen() {
        if (!suggestSavingChanges()) return
        val file = chooseFile(I18N.messages.getString("MainView.File.Open"), FileChooserMode.Single) ?: return
        open(file)
    }

    fun open(file: File) {
        loadAsync(file) addOnSuccess {
            openedAutomaton = it.createAutomaton()
            openedFile = file
        }
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
            chooseFile(I18N.messages.getString("MainView.File.SaveAs"), FileChooserMode.Save)
                ?: return false
        )
        return true
    }

    private fun saveAs(file: File) {
        saveAsync(file)
    }

    fun chooseFile(title: String, mode: FileChooserMode): File? =
        chooseFile(
            title = title,
            filters = automatonSerializers().map { it.extensionFilter }.toTypedArray(),
            initialDirectory = openedFile?.parentFile ?: defaultDirectory(),
            mode = mode,
            owner = uiComponent.currentWindow,
            initialFileName = openedFile?.name ?: name
        ).firstOrNull()?.let { file ->
            if (mode == FileChooserMode.Save && file.extension.isEmpty())
                File(file.path + automatonSerializers().first().extensionFilter.extensions.first().drop(1))
            else file
        }

    private fun defaultDirectory() = runCatching {
        File("${System.getProperty("user.home")}/Documents/automaton-constructor").takeIf { it.isDirectory || it.mkdirs() }
    }.getOrNull()

    private fun findFileAutomatonSerializer(file: File) =
        automatonSerializers().find { serializer ->
            serializer.extensionFilter.extensions.any { file.path.endsWith(it.drop(1)) }
        } ?: throw IllegalArgumentException(
            MessageFormat.format(
                I18N.messages.getString("OpenedAutomatonController.UnknownFileExtension"),
                file.extension, file
            )
        )

    private fun saveAsync(file: File, serializer: AutomatonSerializer = findFileAutomatonSerializer(file)): Task<Unit> {
        val automatonData = openedAutomaton.getData()
        openedAutomaton.undoRedoManager.wasModified = false
        return uiComponent.runAsyncWithDialog(
            MessageFormat.format(I18N.messages.getString("OpenedAutomatonController.SavingAutomaton"), file),
            daemon = false
        ) {
            serializer.serialize(file, automatonData)
        } addOnSuccess {
            openedFile = file
        } addOnFail {
            openedAutomaton.undoRedoManager.wasModified = true
            throw RuntimeException(
                MessageFormat.format(
                    I18N.messages.getString("OpenedAutomatonController.UnableToSaveAutomaton"),
                    file
                ), it
            )
        } addOnCancel {
            openedAutomaton.undoRedoManager.wasModified = true
        }
    }

    fun loadAsync(
        file: File,
        serializer: AutomatonSerializer = findFileAutomatonSerializer(file)
    ): Task<AutomatonData> =
        uiComponent.runAsyncWithDialog(
            MessageFormat.format(I18N.messages.getString("OpenedAutomatonController.LoadingAutomaton"), file),
            daemon = true
        ) {
            serializer.deserialize(file)
        } addOnFail {
            throw RuntimeException(
                MessageFormat.format(
                    I18N.messages.getString("OpenedAutomatonController.UnableToLoadAutomaton"),
                    file
                ), it
            )
        }

    /**
     * @return false if CANCEL is pressed
     */
    fun suggestSavingChanges(): Boolean {
        if (!openedAutomaton.undoRedoManager.wasModified) return true
        val result = alert(
            Alert.AlertType.CONFIRMATION,
            MessageFormat.format(I18N.messages.getString("OpenedAutomatonController.SuggestSavingChanges"), name),
            null,
            ButtonType(I18N.messages.getString("Dialog.yes.button"), ButtonType.YES.buttonData),
            ButtonType(I18N.messages.getString("Dialog.no.button"), ButtonType.NO.buttonData),
            ButtonType(I18N.messages.getString("Dialog.cancel.button"), ButtonType.CANCEL.buttonData),
            owner = uiComponent.currentWindow,
            title = I18N.messages.getString("Dialog.confirmation")
        ).result
        return if (result.buttonData == ButtonType.YES.buttonData) onSave()
        else result.buttonData != ButtonType.CANCEL.buttonData
    }
}
