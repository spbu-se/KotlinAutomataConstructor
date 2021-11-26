package automaton.constructor.model.memory.tape

import automaton.constructor.model.memory.AbstractMemoryUnit
import automaton.constructor.model.transition.Transition
import automaton.constructor.model.transition.property.TransitionPropertyDescriptor
import automaton.constructor.model.transition.property.createCharOrEpsTransitionPropertyDescriptor
import javafx.collections.ObservableList
import javafx.scene.Node
import javafx.scene.input.KeyCode
import javafx.scene.text.Font
import javafx.scene.text.Text
import org.fxmisc.richtext.InlineCssTextField
import tornadofx.*
import kotlin.math.min

const val BLANK_CHAR = '⬜'

abstract class AbstractTape private constructor(
    name: String,
    headPosition: Int,
    var dataStartPosition: Int,
    val data: ObservableList<Char>,
    val expectedCharPropDescriptor: TransitionPropertyDescriptor<Char?>
) : AbstractMemoryUnit(name) {

    override val filterDescriptors = listOf(expectedCharPropDescriptor)
    override fun getCurrentFilterValues(): List<*> = listOf(getChar(headPosition))

    val headPositionProperty = headPosition.toProperty()
    var headPosition by headPositionProperty

    val dataEndPosition get() = dataStartPosition + data.lastIndex

    var Transition.expectedChar
        get() = get(expectedCharPropDescriptor)
        set(value) = set(expectedCharPropDescriptor, value)

    init {
        shrinkAndExpandData()
        data.onChange { shrinkAndExpandData() }
        headPositionProperty.onChange { shrinkAndExpandData() }
    }

    constructor(name: String, canBeDeemedEpsilon: Boolean) : this(
        name, 0, 0, observableListOf(),
        createCharOrEpsTransitionPropertyDescriptor("Expected char", canBeDeemedEpsilon = canBeDeemedEpsilon)
    )

    constructor(other: AbstractTape) : this(
        other.name,
        other.headPosition,
        other.dataStartPosition,
        other.data.toObservable(),
        other.expectedCharPropDescriptor
    )

    fun getChar(index: Int): Char = data[index - dataStartPosition]

    fun setChar(index: Int, char: Char) {
        data[index - dataStartPosition] = char
    }

    // data should not have BLANK_CHAR prefixes/suffixes unless they are required to make headPosition to be within data
    // headPosition should be within data
    private fun shrinkAndExpandData() {
        if (data.indexOfFirst { it != BLANK_CHAR } == -1) {
            dataStartPosition = headPosition
            if (data != listOf(BLANK_CHAR)) data.setAll(mutableListOf(BLANK_CHAR))
            return
        }
        if (data.first() == BLANK_CHAR && dataStartPosition < headPosition) {
            val removedPrefixLength = min(data.indexOfFirst { it != BLANK_CHAR }, headPosition - dataStartPosition)
            println(removedPrefixLength)
            dataStartPosition += removedPrefixLength
            data.remove(0, removedPrefixLength)
        }
        if (data.last() == BLANK_CHAR && dataEndPosition > headPosition) {
            val removedSuffixLength = min(
                data.lastIndex - data.indexOfLast { it != BLANK_CHAR },
                dataEndPosition - headPosition
            )
            data.remove(data.size - removedSuffixLength, data.size)
        }
        if (dataStartPosition > headPosition) {
            val addedPrefixLength = dataStartPosition - headPosition
            dataStartPosition = headPosition
            data.addAll(0, List(addedPrefixLength) { BLANK_CHAR })
        }
        if (dataEndPosition < headPosition) {
            val addedSuffixLength = headPosition - dataEndPosition
            data.addAll(List(addedSuffixLength) { BLANK_CHAR })
        }
    }

    // TODO move implementation somewhere else
    override fun createEditor(): Node = InlineCssTextField().apply {
        undoManager = null
        val fontFamily = "monospace"
        val fontSize = 16.0
        minHeight = Text().apply { font = Font.font(fontFamily, fontSize) }.boundsInLocal.height + 10.0
        fun updateEditor() {
            shrinkAndExpandData()
            if (data == listOf(BLANK_CHAR)) {
                text = ""
                return
            }
            val oldCaretPosition = caretPosition
            text = data.toCharArray().concatToString()
            if (length == 0) return
            moveTo(oldCaretPosition.coerceAtMost(length - 1))
            val headPositionInData = (headPosition - dataStartPosition).coerceAtMost(length - 1)
            val commonStyle = "-fx-font-family: $fontFamily; -fx-font-size: $fontSize;"
            clearStyle(0, length)
            setStyle(0, headPositionInData, "$commonStyle; -fx-strikethrough: true;")
            setStyle(
                headPositionInData,
                headPositionInData + 1,
                "$commonStyle; -fx-fill: blue; -rtfx-background-color: darkorange;"
            )
            setStyle(headPositionInData + 1, length, commonStyle)
        }
        updateEditor()
        data.onChange { updateEditor() }
        headPositionProperty.onChange { updateEditor() }
        textProperty().onChange {
            val newData = text.toMutableList()
            if (data != newData && (data != listOf(BLANK_CHAR) || newData != emptyList<Char>()))
                data.setAll(newData)
        }
        setOnKeyPressed {
            if (it.code == KeyCode.UP) headPosition++
            else if (it.code == KeyCode.DOWN) headPosition--
        }
    }
}
