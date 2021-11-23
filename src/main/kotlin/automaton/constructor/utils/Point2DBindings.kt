package automaton.constructor.utils

import javafx.beans.value.ObservableValue
import javafx.geometry.Point2D
import tornadofx.*

val ObservableValue<Point2D>.x get() = doubleBinding { it!!.x }
val ObservableValue<Point2D>.y get() = doubleBinding { it!!.y }
