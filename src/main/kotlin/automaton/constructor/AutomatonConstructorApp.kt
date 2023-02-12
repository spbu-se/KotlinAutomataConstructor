package automaton.constructor

import automaton.constructor.controller.LocaleController
import automaton.constructor.utils.I18N
import automaton.constructor.view.MainWindow
import javafx.scene.control.Alert
import javafx.scene.control.CheckBox
import javafx.stage.Stage
import tornadofx.*
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.text.MessageFormat

class AutomatonConstructorApp : App() {
    override fun start(stage: Stage) {
        FX.stylesheets.add("style.css")
        super.start(stage)
        find<LocaleController>()
        val unnamedParams = parameters.unnamed
        MainWindow().apply {
            show()
            if (unnamedParams.isNotEmpty()) {
                val path = unnamedParams.joinToString("")
                val file = File(path)
                if (file.exists()) fileController.open(file)
                else runLater {
                    error(
                        title=I18N.messages.getString("Dialog.error"),
                        header=MessageFormat.format(I18N.messages.getString("Error.FailedToOpenFile.Header"), path),
                        content=I18N.messages.getString("Error.FailedToOpenFile.Content")
                    )
                }
            }
        }

        if (config.boolean("showStartUpHint", true))
            Alert(Alert.AlertType.INFORMATION).apply {
                title = I18N.messages.getString("Dialog.information")
                headerText = I18N.messages.getString("Hint.UseRightClickToAddElements")
                dialogPane.expandableContent = CheckBox(I18N.messages.getString("Hint.DontShowAgain")).apply {
                    setOnAction {
                        config["showStartUpHint"] = !isSelected
                        config.save()
                    }
                }
                dialogPane.isExpanded = true
            }.show()
    }

    override val configBasePath: Path
        get() = Paths.get(System.getProperty("user.home"), "automaton-constructor-conf")
}

fun main(args: Array<String>) = launch<AutomatonConstructorApp>(args)
