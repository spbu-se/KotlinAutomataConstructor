package automaton.constructor.model.property

import automaton.constructor.utils.MostlyGeneratedOrInline
import automaton.constructor.utils.setControlNewText
import javafx.scene.control.*
import javafx.util.StringConverter

object DynamicPropertyDescriptors {
    const val EPSILON_CHAR = 'ε'
    const val EPSILON_STRING = EPSILON_CHAR.toString()
    const val BLANK_CHAR = '□'


    fun outputChar(name: String) = charOrEps(
        name = name,
        canBeDeemedEpsilon = false,
        displayValueFactory = { if (it == EPSILON_VALUE) "" else it.toString() }
    )

    fun charOrEps(
        name: String,
        canBeDeemedEpsilon: Boolean,
        displayValueFactory: ((Char?) -> String)? = null
    ): DynamicPropertyDescriptor<Char?> = charOrElse(
        name = name,
        canBeDeemedEpsilon = canBeDeemedEpsilon,
        emptyChar = EPSILON_CHAR,
        emptyValue = EPSILON_VALUE,
        charConverter = { it },
        displayValueFactory = displayValueFactory
    )

    fun charOrBlank(
        name: String,
        displayValueFactory: ((Char) -> String)? = null
    ): DynamicPropertyDescriptor<Char> = charOrElse(
        name = name,
        canBeDeemedEpsilon = false,
        emptyChar = BLANK_CHAR,
        emptyValue = BLANK_CHAR,
        charConverter = { it },
        displayValueFactory = displayValueFactory
    )

    @MostlyGeneratedOrInline
    private inline fun <T> charOrElse(
        name: String,
        canBeDeemedEpsilon: Boolean,
        emptyChar: Char,
        emptyValue: T,
        crossinline charConverter: (Char) -> T,
        noinline displayValueFactory: ((T) -> String)? = null
    ) = DynamicPropertyDescriptor(
        displayName = name,
        defaultValue = emptyValue,
        canBeDeemedEpsilon = canBeDeemedEpsilon,
        editorFactory = { property ->
            TextField().apply {
                textFormatter = TextFormatter(property.descriptor.stringConverter, emptyValue) { change ->
                    change.apply {
                        if (text.length == 1) setControlNewText(text)
                        else if (controlNewText.length != 1) setControlNewText(emptyChar.toString())
                    }
                }.apply { valueProperty().bindBidirectional(property) }
            }
        },
        stringConverter = object : StringConverter<T>() {
            override fun toString(obj: T): String =
                if (obj == EPSILON_VALUE) emptyChar.toString() else obj.toString()

            override fun fromString(string: String): T =
                if (string == emptyChar.toString()) emptyValue else charConverter(string.single())
        },
        displayValueFactory = displayValueFactory ?: {
            if (it == EPSILON_VALUE) emptyChar.toString() else it.toString()
        }
    )

    @MostlyGeneratedOrInline
    inline fun <reified E : Enum<E>> enum(name: String, noinline displayValueFactory: ((E) -> String)? = null) =
        choice(
            name = name,
            canBeDeemedEpsilon = false,
            values = enumValues(),
            displayValueFactory = displayValueFactory
        )

    fun <T> choice(
        name: String,
        canBeDeemedEpsilon: Boolean,
        vararg values: T,
        displayValueFactory: ((T) -> String)? = null
    ): DynamicPropertyDescriptor<T> {
        val stringToValueMap = values.associateBy { it.toString() }
        return DynamicPropertyDescriptor(
            displayName = name,
            defaultValue = values.first(),
            canBeDeemedEpsilon = canBeDeemedEpsilon,
            editorFactory = { property ->
                ChoiceBox<T>().apply {
                    converter = property.descriptor.stringConverter
                    items.setAll(stringToValueMap.values)
                    valueProperty().bindBidirectional(property)
                }
            },
            stringConverter = object : StringConverter<T>() {
                override fun toString(obj: T): String = obj.toString()
                override fun fromString(string: String): T = stringToValueMap.getValue(string)
            },
            displayValueFactory = displayValueFactory ?: { it.toString() }
        )
    }

    fun stringOrEps(
        name: String,
        canBeDeemedEpsilon: Boolean,
        displayValueFactory: ((String?) -> String)? = null
    ) = DynamicPropertyDescriptor(
        displayName = name,
        canBeDeemedEpsilon = canBeDeemedEpsilon,
        defaultValue = null,
        editorFactory = { property ->
            TextField().apply {
                textFormatter = TextFormatter(property.descriptor.stringConverter, EPSILON_VALUE) { change ->
                    change.apply {
                        if (controlNewText.isEmpty()) setControlNewText(EPSILON_STRING)
                        else if (controlText == EPSILON_STRING && text.isNotEmpty()) setControlNewText(text)
                    }
                }.apply { valueProperty().bindBidirectional(property) }
            }
        },
        stringConverter = object : StringConverter<String?>() {
            override fun toString(obj: String?): String = if (obj == EPSILON_VALUE) EPSILON_STRING else obj
            override fun fromString(string: String): String? = if (string == EPSILON_STRING) EPSILON_VALUE else string
        },
        displayValueFactory = displayValueFactory ?: { if (it == EPSILON_VALUE) EPSILON_STRING else it }
    )
}
