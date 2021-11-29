package automaton.constructor.model.transition.property

import automaton.constructor.utils.setControlNewText
import javafx.scene.control.ChoiceBox
import javafx.scene.control.TextField
import javafx.scene.control.TextFormatter
import javafx.util.StringConverter

const val EPSILON_CHAR = 'ε'
const val EPSILON_STRING = EPSILON_CHAR.toString()
const val BLANK_CHAR = '□'

fun createCharOrEpsTransitionPropertyDescriptor(
    name: String,
    canBeDeemedEpsilon: Boolean
): TransitionPropertyDescriptor<Char?> =
    createCharOrElseTransitionPropertyDescriptor(name, canBeDeemedEpsilon, EPSILON_CHAR, EPSILON_VALUE) { it }

fun createCharOrBlankTransitionPropertyDescriptor(name: String): TransitionPropertyDescriptor<Char> =
    createCharOrElseTransitionPropertyDescriptor(name, false, BLANK_CHAR, BLANK_CHAR) { it }

private inline fun <T> createCharOrElseTransitionPropertyDescriptor(
    name: String,
    canBeDeemedEpsilon: Boolean,
    emptyChar: Char,
    emptyValue: T,
    crossinline charConverter: (Char) -> T
) = TransitionPropertyDescriptor(
    displayName = name,
    defaultValue = emptyValue,
    canBeDeemedEpsilon = canBeDeemedEpsilon,
    editorFactory = { property ->
        TextField().apply {
            textFormatter = TextFormatter(object : StringConverter<T>() {
                override fun toString(obj: T): String = property.descriptor.stringifyValue(obj)
                override fun fromString(string: String): T =
                    if (string == emptyChar.toString()) emptyValue else charConverter(string.single())
            }, emptyValue) { change ->
                change.apply {
                    if (text.length == 1) setControlNewText(text)
                    else if (controlNewText.length != 1) setControlNewText(emptyChar.toString())
                }
            }.apply { valueProperty().bindBidirectional(property) }
        }
    },
    stringifier = { if (it == EPSILON_VALUE) emptyChar.toString() else it.toString() }
)

inline fun <reified E : Enum<E>> createEnumTransitionPropertyDescriptor(name: String) =
    createChoiceTransitionPropertyDescriptor(name, false, *enumValues<E>())

fun <T> createChoiceTransitionPropertyDescriptor(
    name: String,
    canBeDeemedEpsilon: Boolean,
    vararg values: T
): TransitionPropertyDescriptor<T> {
    val stringToValueMap = values.associateBy { it.toString() }
    return TransitionPropertyDescriptor(
        displayName = name,
        defaultValue = values.first(),
        canBeDeemedEpsilon = canBeDeemedEpsilon,
        editorFactory = { property ->
            ChoiceBox<T>().apply {
                converter = object : StringConverter<T>() {
                    override fun toString(obj: T): String = property.descriptor.stringifyValue(obj)
                    override fun fromString(string: String): T = stringToValueMap.getValue(string)
                }
                items.setAll(stringToValueMap.values)
                valueProperty().bindBidirectional(property)
            }
        },
        stringifier = { it.toString() }
    )
}

fun createStringOrEpsTransitionPropertyDescriptor(
    name: String,
    canBeDeemedEpsilon: Boolean
) = TransitionPropertyDescriptor<String?>(
    name,
    canBeDeemedEpsilon = canBeDeemedEpsilon,
    defaultValue = null,
    editorFactory = { property ->
        TextField().apply {
            textFormatter = TextFormatter(object : StringConverter<String?>() {
                override fun toString(obj: String?) = property.descriptor.stringifyValue(obj)
                override fun fromString(string: String) = if (string == EPSILON_STRING) EPSILON_VALUE else string
            }, EPSILON_VALUE) { change ->
                change.apply {
                    if (controlNewText.isEmpty()) setControlNewText(EPSILON_STRING)
                    else if (controlText == EPSILON_STRING && text.isNotEmpty()) setControlNewText(text)
                }
            }.apply { valueProperty().bindBidirectional(property) }
        }
    },
    stringifier = { if (it == EPSILON_VALUE) EPSILON_STRING else it }
)
