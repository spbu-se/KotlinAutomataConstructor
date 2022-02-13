package automaton.constructor.model.memory.tape

import automaton.constructor.model.property.DynamicPropertyDescriptors.BLANK_CHAR
import automaton.constructor.utils.monospaced
import automaton.constructor.utils.scrollToRightWhenUnfocused
import automaton.constructor.utils.setControlNewText
import automaton.constructor.utils.withoutPadding
import javafx.geometry.Pos
import javafx.scene.control.TextField
import javafx.scene.control.TextFormatter
import javafx.util.converter.CharacterStringConverter
import tornadofx.*

const val DISPLAYED_CHARS_ON_EACH_SIDE = 7

class Track private constructor(
    processedChars: List<Char>,
    unprocessedChars: List<Char>
) {
    val processedChars = processedChars.toObservable()
    val unprocessedChars = unprocessedChars.toObservable()
    var currentChar
        get() = unprocessedChars.firstOrNull() ?: BLANK_CHAR
        set(value) {
            if (unprocessedChars.isEmpty()) unprocessedChars.add(value)
            else unprocessedChars[0] = value
        }

    constructor(initValue: String) : this(emptyList(), initValue.toList())

    constructor(other: Track) : this(other.processedChars, other.unprocessedChars)

    fun shiftHead(shift: Int) {
        if (shift > 0) {
            processedChars.addAll(
                (unprocessedChars.take(shift) + List((shift - unprocessedChars.size).coerceAtLeast(0)) { BLANK_CHAR }).toMutableList()
            )
            unprocessedChars.remove(0, shift.coerceAtMost(unprocessedChars.size))
        } else if (shift < 0) {
            unprocessedChars.addAll(0,
                (List((-shift - processedChars.size).coerceAtLeast(0)) { BLANK_CHAR } + processedChars.takeLast(-shift)).toMutableList()
            )
            processedChars.remove(
                processedChars.size - (-shift).coerceAtMost(processedChars.size),
                processedChars.size
            )
        }
    }

    fun createProcessedCharsEditor() = TextField().apply {
        alignment = Pos.CENTER_RIGHT
        prefColumnCount = DISPLAYED_CHARS_ON_EACH_SIDE
        withoutPadding()
        monospaced()
        scrollToRightWhenUnfocused()
        text = processedChars.toCharArray().concatToString()
        processedChars.onChange { text = processedChars.toCharArray().concatToString() }
        textProperty().onChange {
            val newProcessed = text.toMutableList()
            if (newProcessed != processedChars) processedChars.setAll(newProcessed)
        }
    }

    fun createUnprocessedCharsEditor() = TextField().apply {
        alignment = Pos.CENTER_LEFT
        prefColumnCount = DISPLAYED_CHARS_ON_EACH_SIDE
        withoutPadding()
        monospaced()
        text = unprocessedChars.drop(1).toCharArray().concatToString()
        unprocessedChars.onChange { text = unprocessedChars.drop(1).toCharArray().concatToString() }
        textProperty().onChange {
            val newUnprocessed = text.toMutableList().apply { add(0, currentChar) }
            if (newUnprocessed != unprocessedChars) unprocessedChars.setAll(newUnprocessed)
        }
    }

    fun createCurrentCharEditor() = TextField(currentChar.toString()).apply {
        alignment = Pos.CENTER
        prefColumnCount = 1
        withoutPadding()
        monospaced()
        textFormatter = TextFormatter(CharacterStringConverter(), currentChar) { change ->
            change.takeIf { it.text.length == 1 }?.apply { setControlNewText(change.text) }
        }.apply { valueProperty().onChange { if (currentChar != value) currentChar = value } }
        unprocessedChars.onChange { text = currentChar.toString() }
    }
}
