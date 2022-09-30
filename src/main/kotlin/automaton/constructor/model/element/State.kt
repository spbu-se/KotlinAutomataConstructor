package automaton.constructor.model.element

import automaton.constructor.model.memory.MemoryUnitDescriptor
import automaton.constructor.model.property.DynamicPropertyDescriptorGroup
import javafx.geometry.Point2D

/**
 * A state of the automaton
 */
class State(
    memoryDescriptors: List<MemoryUnitDescriptor>,
    name: String,
    position: Point2D
) : AutomatonVertex(
    memoryDescriptors,
    propertyDescriptorGroups = memoryDescriptors.map {
        DynamicPropertyDescriptorGroup(
            displayName = it.displayName,
            filters = emptyList(),
            sideEffects = it.stateSideEffects
        )
    },
    name,
    position
)
