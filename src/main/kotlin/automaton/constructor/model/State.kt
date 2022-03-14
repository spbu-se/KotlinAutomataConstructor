package automaton.constructor.model

import automaton.constructor.model.memory.MemoryUnitDescriptor
import automaton.constructor.model.property.AutomatonElement
import automaton.constructor.model.property.DynamicPropertyDescriptorGroup
import javafx.beans.property.BooleanProperty
import javafx.beans.property.Property
import javafx.geometry.Point2D
import tornadofx.*

/**
 * A state of the automaton
 */
class State(name: String, position: Point2D, memoryDescriptors: List<MemoryUnitDescriptor>) :
    AutomatonElement(memoryDescriptors.map {
        DynamicPropertyDescriptorGroup(
            it,
            it.stateFilters,
            it.stateSideEffects
        )
    }) {
    val nameProperty: Property<String> = name.toProperty()
    var name: String by nameProperty
    val isInitialProperty: BooleanProperty = false.toProperty()
    var isInitial by isInitialProperty
    val isFinalProperty: BooleanProperty = false.toProperty()
    var isFinal by isFinalProperty

    /**
     * `true` if there exists running execution state in this state
     */
    val isCurrentProperty: BooleanProperty = false.toProperty()
    var isCurrent by isCurrentProperty
    val positionProperty: Property<Point2D> = position.toProperty()
    var position: Point2D by positionProperty
    val lastReleasePositionProperty: Property<Point2D> = position.toProperty().apply {
        onChange { this@State.position = it!! }
    }
    var lastReleasePosition: Point2D by lastReleasePositionProperty
    override val undoRedoProperties
        get() = super.undoRedoProperties +
                listOf(nameProperty, isInitialProperty, isFinalProperty, lastReleasePositionProperty)
}
