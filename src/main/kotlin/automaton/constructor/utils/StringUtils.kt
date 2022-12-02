package automaton.constructor.utils

import java.util.*

fun String.capitalize(locale: Locale = I18N.locale): String =
    replaceFirstChar { it.titlecase(locale) }
