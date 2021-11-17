package automaton.constructor.utils

import javafx.scene.control.TextFormatter
import javafx.scene.text.Text
import tornadofx.*

fun Text.translateToCenter() {
    translateXProperty().bind(layoutBoundsProperty().doubleBinding { -it!!.width / 2 })
    translateYProperty().bind(layoutBoundsProperty().doubleBinding { it!!.height / 4 })
}

fun TextFormatter.Change.setControlNewText(text: String) {
    this.text = text
    setRange(0, controlText.length)
}
