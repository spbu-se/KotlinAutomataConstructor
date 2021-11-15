package automaton.constructor.model

import javafx.beans.property.BooleanProperty
import javafx.beans.property.Property
import javafx.geometry.Point2D
import tornadofx.*

class State(name: String = "", position: Point2D = Point2D.ZERO) {
    val nameProperty: Property<String> = name.toProperty()
    var name: String by nameProperty
    val isInitialProperty: BooleanProperty = false.toProperty()
    var isInitial by isInitialProperty
    val isFinalProperty: BooleanProperty = false.toProperty()
    var isFinal by isFinalProperty
    val isCurrentProperty: BooleanProperty = false.toProperty()
    var isCurrent by isCurrentProperty
    val positionProperty: Property<Point2D> = position.toProperty()
    var position: Point2D by positionProperty
}
