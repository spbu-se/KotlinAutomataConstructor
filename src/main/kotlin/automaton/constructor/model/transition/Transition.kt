package automaton.constructor.model.transition

import automaton.constructor.model.State
import automaton.constructor.model.memory.MemoryUnitDescriptor
import automaton.constructor.model.property.AutomatonElement
import automaton.constructor.model.property.DynamicProperty
import automaton.constructor.model.property.DynamicPropertyDescriptorGroup
import automaton.constructor.model.property.EPSILON_VALUE

/**
 * A transition between states of the automaton
 */
class Transition(
    val source: State,
    val target: State,
    /**
     * Memory unit descriptors of the automaton that has this transition
     */
    memoryDescriptors: List<MemoryUnitDescriptor>
) : AutomatonElement(memoryDescriptors.map {
    DynamicPropertyDescriptorGroup(
        it,
        it.transitionFilters,
        it.transitionSideEffects
    )
}) {
    val transitionFilters = super.filters
    val transitionSideEffects = super.sideEffects
    val transitionAllProperties = super.allProperties

    override val filters: List<DynamicProperty<*>> = super.filters + target.filters
    override val sideEffects: List<DynamicProperty<*>> = super.sideEffects + target.sideEffects
    override val allProperties: Collection<DynamicProperty<*>> = super.allProperties + target.allProperties

    /**
     * Transition is pure if all its properties have [EPSILON_VALUE] (including both filters and sideEffects)
     *
     * Pure transition is always possible regardless of memory data and has no effect on the memory data
     *
     * Executor may take pure transition without asking nor informing memory units
     */
    fun isPure() = allProperties.all { it.value == EPSILON_VALUE }
}
