package automaton.constructor

import automaton.constructor.controller.SettingsController
import automaton.constructor.utils.I18N
import automaton.constructor.view.MainWindow
import javafx.scene.control.Alert
import javafx.scene.control.CheckBox
import javafx.stage.Stage
import javafx.stage.Window
import javafx.util.Duration
import tornadofx.*
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.text.MessageFormat

class AutomatonConstructorApp : App() {
    private val settingsController by inject<SettingsController>()

    override fun start(stage: Stage) {
        FX.stylesheets.add("style.css")
        super.start(stage)
        SettingsController().initGlobalLocale()
        val unnamedParams = parameters.unnamed
        MainWindow().apply {
            show()
            if (unnamedParams.isEmpty()) fileController.onNew()
            else {
                val path = unnamedParams.joinToString("")
                val file = File(path)
                if (file.exists()) fileController.open(file)
                else runLater {
                    error(
                        title=I18N.messages.getString("Dialog.error"),
                        header=MessageFormat.format(I18N.messages.getString("Error.FailedToOpenFile.Header"), path),
                        content=I18N.messages.getString("Error.FailedToOpenFile.Content"),
                        owner = currentWindow
                    )
                }
            }
            runLater(Duration.millis(100.0)) {
                modalStage?.let {
                    if (it.isFocused) showStartUpHintIfNeeded(currentWindow)
                    else it.focusedProperty().onChangeOnce { showStartUpHintIfNeeded(currentWindow) }
                } ?: showStartUpHintIfNeeded(currentWindow)
            }
        }
    }

    private fun showStartUpHintIfNeeded(owner: Window?) {
        if (settingsController.isHintEnabled(SettingsController.Hint.STARTUP))
            Alert(Alert.AlertType.INFORMATION).apply {
                initOwner(owner)
                title = I18N.messages.getString("Dialog.information")
                headerText = I18N.messages.getString("Hint.UseRightClickToAddElements")
                dialogPane.expandableContent = CheckBox(I18N.messages.getString("Hint.DontShowAgain"))
                    .also { dontShowCheckbox ->
                        dontShowCheckbox.setOnAction {
                            settingsController.setHintEnabled(SettingsController.Hint.STARTUP, !dontShowCheckbox.isSelected)
                        }
                    }
                dialogPane.isExpanded = true
            }.show()
    }

    override val configBasePath: Path
        get() = Paths.get(System.getProperty("user.home"), "automaton-constructor-conf")
}

fun main(args: Array<String>) = launch<AutomatonConstructorApp>(args)
