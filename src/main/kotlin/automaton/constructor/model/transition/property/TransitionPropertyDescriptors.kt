package automaton.constructor.model.transition.property

import automaton.constructor.utils.setControlNewText
import javafx.scene.control.TextField
import javafx.scene.control.TextFormatter
import javafx.util.StringConverter

const val EPSILON_STRING = "ε"

fun createCharOrEpsTransitionPropertyDescriptor(name: String, canBeDeemedEpsilon: Boolean) =
    TransitionPropertyDescriptor<Char?>(
        name = name,
        defaultValue = EPSILON_VALUE,
        canBeDeemedEpsilon = canBeDeemedEpsilon,
        settingControlFactory = { property ->
            TextField().apply {
                textFormatter = TextFormatter(object : StringConverter<Char?>() {
                    override fun toString(obj: Char?): String = property.descriptor.stringifyValue(obj)
                    override fun fromString(string: String): Char? =
                        if (string == EPSILON_STRING) EPSILON_VALUE else string.single()
                }, EPSILON_VALUE) { change ->
                    change.apply {
                        if (text.length == 1) setControlNewText(text)
                        else if (controlNewText.length != 1) setControlNewText(EPSILON_STRING)
                    }
                }.apply { valueProperty().bindBidirectional(property) }
            }
        },
        stringifier = { it?.toString() ?: EPSILON_STRING }
    )
