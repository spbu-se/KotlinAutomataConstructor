package automaton.constructor

import automaton.constructor.controller.LocaleController
import automaton.constructor.utils.I18N
import automaton.constructor.view.MainWindow
import javafx.scene.control.*
import javafx.stage.Stage
import tornadofx.*

class AutomatonConstructorApp : App() {
    override fun start(stage: Stage) {
        FX.stylesheets.add("style.css")
        super.start(stage)
        find<LocaleController>()
        MainWindow().show()
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
}

fun main(args: Array<String>) = launch<AutomatonConstructorApp>(args)
