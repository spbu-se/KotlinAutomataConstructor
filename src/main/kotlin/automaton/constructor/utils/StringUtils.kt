package automaton.constructor.utils

import java.util.*

fun String.capitalize() = replaceFirstChar { it.titlecase(Locale.getDefault()) }
