package automaton.constructor.utils

import kotlin.math.round

fun Double.roundTo(step: Int) = round(this / step) * step
