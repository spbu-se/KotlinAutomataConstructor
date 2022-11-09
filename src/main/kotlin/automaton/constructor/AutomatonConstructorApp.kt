package automaton.constructor

import automaton.constructor.view.MainWindow
import javafx.stage.Stage
import tornadofx.*

class AutomatonConstructorApp : App() {
    override fun start(stage: Stage) {
        FX.stylesheets.add("style.css")
        super.start(stage)
        MainWindow().show()
    }
}

fun main(args: Array<String>) = launch<AutomatonConstructorApp>(args)
