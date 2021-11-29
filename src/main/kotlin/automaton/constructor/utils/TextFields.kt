package automaton.constructor.utils

import javafx.scene.control.TextField
import javafx.scene.text.Font
import tornadofx.*

fun TextField.withoutPadding() = apply {
    paddingAll = 0.0
}

fun TextField.monospaced() = apply {
    font = Font.font("monospace", 16.0)
}

fun TextField.scrollToRightWhenUnfocused() = apply {
    fun fixCaretPosition() {
        if (!isFocused) runLater { positionCaret(length) }
    }
    textProperty().onChange { fixCaretPosition() }
    focusedProperty().onChange { fixCaretPosition() }
    caretPositionProperty().onChange { fixCaretPosition() }
}
