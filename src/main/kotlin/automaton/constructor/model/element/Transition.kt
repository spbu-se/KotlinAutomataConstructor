package automaton.constructor.model.element

import automaton.constructor.model.memory.MemoryUnitDescriptor
import automaton.constructor.model.property.DynamicPropertyDescriptorGroup
import javafx.beans.property.Property
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Point2D
import tornadofx.*

/**
 * A transition between states of the automaton
 */
class Transition(
    val source: AutomatonVertex,
    val target: AutomatonVertex,
    /**
     * Memory unit descriptors of the automaton that has this transition
     */
    memoryDescriptors: List<MemoryUnitDescriptor>
) : AutomatonElement(memoryDescriptors.map {
    DynamicPropertyDescriptorGroup(
        it.displayName,
        it.transitionFilters,
        it.transitionSideEffects
    )
}) {
    override fun isPure() = source is State && super.isPure() && target.isPure()

    /**
     * Transition is a loop if its source is equal to its target.
     */
    fun isLoop() = source == target

    val positionProperty: Property<Point2D?> = SimpleObjectProperty<Point2D?>(null)
    var position: Point2D? by positionProperty
    override val undoRedoProperties: Collection<Property<*>> get() = super.undoRedoProperties + listOf(positionProperty)

    fun resetPosition() {
        position = null
    }
}
