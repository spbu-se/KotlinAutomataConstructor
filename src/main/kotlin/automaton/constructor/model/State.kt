package automaton.constructor.model

import automaton.constructor.model.memory.MemoryUnitDescriptor
import automaton.constructor.model.module.executor.ExecutionState
import automaton.constructor.model.module.executor.ExecutionStatus
import automaton.constructor.model.property.AutomatonElement
import automaton.constructor.model.property.DynamicPropertyDescriptorGroup
import automaton.constructor.utils.filteredSet
import javafx.beans.binding.Bindings.isNotEmpty
import javafx.beans.binding.BooleanBinding
import javafx.beans.property.BooleanProperty
import javafx.beans.property.Property
import javafx.geometry.Point2D
import tornadofx.*

/**
 * A state of the automaton
 */
class State(
    name: String,
    position: Point2D,
    memoryDescriptors: List<MemoryUnitDescriptor>
) : AutomatonElement(
    propertyDescriptorGroups = memoryDescriptors.map {
        DynamicPropertyDescriptorGroup(
            memoryUnitDescriptor = it,
            filters = it.stateFilters,
            sideEffects = it.stateSideEffects
        )
    }
) {
    val nameProperty: Property<String> = name.toProperty()
    var name: String by nameProperty

    val isInitialProperty: BooleanProperty = false.toProperty()
    var isInitial by isInitialProperty

    val isFinalProperty: BooleanProperty = false.toProperty()
    var isFinal by isFinalProperty

    val positionProperty: Property<Point2D> = position.toProperty()
    var position: Point2D by positionProperty

    val lastReleasePositionProperty: Property<Point2D> = position.toProperty().apply {
        onChange { this@State.position = it!! }
    }
    var lastReleasePosition: Point2D by lastReleasePositionProperty

    override val undoRedoProperties
        get() = super.undoRedoProperties +
                listOf(nameProperty, isInitialProperty, isFinalProperty, lastReleasePositionProperty)

    val executionStates = observableSetOf<ExecutionState>()

    /**
     * Contains `true` if there exists running execution state in this state
     */
    val isCurrentBinding: BooleanBinding = isNotEmpty(executionStates.filteredSet {
        it.statusProperty.isEqualTo(ExecutionStatus.RUNNING)
    })
    val isCurrent by isCurrentBinding


    companion object {
        const val RADIUS = 50.0
    }
}
