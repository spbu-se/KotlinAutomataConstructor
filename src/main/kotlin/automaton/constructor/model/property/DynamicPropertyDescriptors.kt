package automaton.constructor.model.property

import automaton.constructor.utils.setControlNewText
import javafx.scene.control.ChoiceBox
import javafx.scene.control.TextField
import javafx.scene.control.TextFormatter
import javafx.util.StringConverter

object DynamicPropertyDescriptors {
    const val EPSILON_CHAR = 'ε'
    const val EPSILON_STRING = EPSILON_CHAR.toString()
    const val BLANK_CHAR = '□'

    fun charOrEps(
        name: String,
        canBeDeemedEpsilon: Boolean
    ): DynamicPropertyDescriptor<Char?> =
        charOrElse(name, canBeDeemedEpsilon, EPSILON_CHAR, EPSILON_VALUE) { it }

    fun charOrBlank(name: String): DynamicPropertyDescriptor<Char> =
        charOrElse(name, false, BLANK_CHAR, BLANK_CHAR) { it }

    private inline fun <T> charOrElse(
        name: String,
        canBeDeemedEpsilon: Boolean,
        emptyChar: Char,
        emptyValue: T,
        crossinline charConverter: (Char) -> T
    ) = DynamicPropertyDescriptor(
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

    inline fun <reified E : Enum<E>> enum(name: String) =
        choice(name, false, *enumValues<E>())

    fun <T> choice(
        name: String,
        canBeDeemedEpsilon: Boolean,
        vararg values: T
    ): DynamicPropertyDescriptor<T> {
        val stringToValueMap = values.associateBy { it.toString() }
        return DynamicPropertyDescriptor(
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

    fun stringOrEps(
        name: String,
        canBeDeemedEpsilon: Boolean
    ) = DynamicPropertyDescriptor<String?>(
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
}
