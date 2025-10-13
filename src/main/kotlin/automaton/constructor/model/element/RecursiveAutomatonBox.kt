package automaton.constructor.model.element

import automaton.constructor.model.automaton.Automaton
import automaton.constructor.model.memory.MemoryUnitDescriptor
import javafx.geometry.Point2D

class RecursiveAutomatonBox(
    memoryDescriptors: List<MemoryUnitDescriptor>,
    override val subAutomaton: Automaton,
    name: String,
    position: Point2D,
    val registerSubManager: Boolean = true
) : AutomatonVertex(memoryDescriptors, propertyDescriptorGroups = emptyList(), name, position), HasSubAutomaton {
    override fun isPure() = false

    init {
        // Disallow being marked as initial or final
        if (isInitial) isInitial = false
        if (isFinal) isFinal = false
        isInitialProperty.addListener { _, _, newValue -> if (newValue) isInitial = false }
        isFinalProperty.addListener { _, _, newValue -> if (newValue) isFinal = false }
    }
}
