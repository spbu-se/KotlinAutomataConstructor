package automaton.constructor.model.module.tape

import automaton.constructor.model.property.DynamicPropertyDescriptors
import automaton.constructor.model.property.DynamicPropertyDescriptors.BLANK_CHAR
import automaton.constructor.utils.monospaced
import automaton.constructor.utils.scrollToRightWhenUnfocused
import automaton.constructor.utils.withoutPadding
import automaton.constructor.utils.I18N.messages
import javafx.geometry.Pos
import javafx.scene.control.TextField
import tornadofx.*

const val DISPLAYED_CHARS_ON_EACH_SIDE = 7

class Track private constructor(
    processed: String,
    current: Char,
    unprocessed: String
) {
    val processedProperty = processed.toProperty()
    var processed: String by processedProperty

    // dynamic property is used to reuse charOrBlank editor implementation
    val currentProperty =
        DynamicPropertyDescriptors.charOrBlank(messages.getString("Track.CurrentProperty")).createProperty().apply {
            value = current
        }
    var current: Char by currentProperty

    // without current
    val unprocessedProperty = unprocessed.toProperty()
    var unprocessed: String by unprocessedProperty

    constructor(initValue: String) : this("", initValue.firstOrNull() ?: BLANK_CHAR, initValue.drop(1))

    constructor(other: Track) : this(other.processed, other.current, other.unprocessed)

    fun moveHead(direction: HeadMoveDirection) = when (direction) {
        HeadMoveDirection.RIGHT -> {
            if (processed.isNotEmpty() || current != BLANK_CHAR) processed += current
            current = unprocessed.firstOrNull() ?: BLANK_CHAR
            unprocessed = unprocessed.drop(1)
        }
        HeadMoveDirection.LEFT -> {
            if (unprocessed.isNotEmpty() || current != BLANK_CHAR) unprocessed = current + unprocessed
            current = processed.lastOrNull() ?: BLANK_CHAR
            processed = processed.dropLast(1)
        }
        HeadMoveDirection.STAGNATE -> Unit
    }

    fun createProcessedCharsEditor() = TextField().apply {
        alignment = Pos.CENTER_RIGHT
        prefColumnCount = DISPLAYED_CHARS_ON_EACH_SIDE
        withoutPadding()
        monospaced()
        scrollToRightWhenUnfocused()
        textProperty().bindBidirectional(processedProperty)
    }

    fun createUnprocessedCharsEditor() = TextField().apply {
        alignment = Pos.CENTER_LEFT
        prefColumnCount = DISPLAYED_CHARS_ON_EACH_SIDE
        withoutPadding()
        monospaced()
        textProperty().bindBidirectional(unprocessedProperty)
    }

    fun createCurrentCharEditor() = (currentProperty.createEditor() as TextField).apply {
        alignment = Pos.CENTER
        prefColumnCount = 1
        withoutPadding()
        monospaced()
    }
}
