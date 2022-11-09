package automaton.constructor.model.element

import automaton.constructor.model.memory.AcceptanceRequiringPolicy
import automaton.constructor.model.memory.MemoryUnitDescriptor
import automaton.constructor.model.module.executor.ExecutionState
import automaton.constructor.model.property.DynamicPropertyDescriptorGroup
import automaton.constructor.utils.filteredSet
import javafx.beans.binding.Bindings.isNotEmpty
import javafx.beans.binding.BooleanBinding
import javafx.beans.property.BooleanProperty
import javafx.beans.property.Property
import javafx.geometry.Point2D
import tornadofx.*

sealed class AutomatonVertex(
    private val memoryDescriptors: List<MemoryUnitDescriptor>,
    propertyDescriptorGroups: List<DynamicPropertyDescriptorGroup>,
    name: String,
    position: Point2D
) : AutomatonElement(propertyDescriptorGroups) {
    val nameProperty: Property<String> = name.toProperty()
    var name: String by nameProperty

    val isInitialProperty: BooleanProperty = false.toProperty()
    var isInitial by isInitialProperty

    val isFinalProperty: BooleanProperty = false.toProperty()
    var isFinal by isFinalProperty

    val positionProperty: Property<Point2D> = position.toProperty()
    var position: Point2D by positionProperty
    val lastReleasePositionProperty: Property<Point2D> = position.toProperty().apply {
        onChange { this@AutomatonVertex.position = it!! }
    }
    var lastReleasePosition: Point2D by lastReleasePositionProperty

    val alwaysEffectivelyFinal
        get() = memoryDescriptors.any { it.acceptanceRequiringPolicy == AcceptanceRequiringPolicy.ALWAYS }

    val executionStates = observableSetOf<ExecutionState>()

    /**
     * Contains `true` if there exists [execution state][executionStates]
     * that [requires processing][ExecutionState.requiresProcessing]
     */
    val isCurrentBinding: BooleanBinding = isNotEmpty(executionStates.filteredSet { it.requiresProcessingBinding })
    val isCurrent by isCurrentBinding

    val isHighlightedProperty = false.toProperty()
    var isHighlighted by isHighlightedProperty

    override val undoRedoProperties
        get() = super.undoRedoProperties +
                listOf(nameProperty, lastReleasePositionProperty, isInitialProperty, isFinalProperty)

    companion object {
        const val RADIUS = 50.0
    }
}
