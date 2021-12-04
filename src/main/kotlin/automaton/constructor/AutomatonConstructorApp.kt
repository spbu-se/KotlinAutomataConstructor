package automaton.constructor

import automaton.constructor.view.MainView
import javafx.stage.Stage
import tornadofx.*

class AutomatonConstructorApp : App(MainView::class) {
    override fun start(stage: Stage) {
        with(stage) {
            width = 1000.0
            height = 600.0
            isMaximized = true
        }
        super.start(stage)
    }
}

fun main(args: Array<String>) {
    if (!System.getProperty("java.version").startsWith("1.8")) {
        println("Invalid java version. Please install Oracle Java 8 to run this software.")
        return
    }
    try {
        Class.forName("javafx.application.Platform")
    } catch (e: ClassNotFoundException) {
        println("Unable to find JavaFX package. Please install it or switch to Oracle Java 8 to run this software.")
        return
    }
    launch<AutomatonConstructorApp>(args)
}
