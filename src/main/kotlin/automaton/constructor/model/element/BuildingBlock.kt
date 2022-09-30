package automaton.constructor.model.element

import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.memory.MemoryUnitDescriptor
import javafx.geometry.Point2D

class BuildingBlock(
    memoryDescriptors: List<MemoryUnitDescriptor>,
    val subAutomaton: Automaton,
    name: String,
    position: Point2D,
) : AutomatonVertex(memoryDescriptors, propertyDescriptorGroups = emptyList(), name, position)
