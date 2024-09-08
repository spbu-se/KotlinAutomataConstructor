package automaton.constructor.utils

import java.util.*

object I18N {
    var messages: ResourceBundle = ResourceBundle.getBundle("messages")
        private set
    var automatonExamples: ResourceBundle = ResourceBundle.getBundle("examples")
        private set

    var locale: Locale
        get() = messages.locale
        set(value) {
            messages = ResourceBundle.getBundle("messages", value)
            automatonExamples = ResourceBundle.getBundle("examples", value)
        }

    val availableLocales = listOf(Locale("en"), Locale("ru"))
}
