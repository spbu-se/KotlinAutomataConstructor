package automaton.constructor.model.transition

import automaton.constructor.model.State
import automaton.constructor.model.memory.MemoryUnitDescriptor
import automaton.constructor.model.transition.property.EPSILON_VALUE
import automaton.constructor.model.transition.property.TransitionProperty
import automaton.constructor.model.transition.property.TransitionPropertyDescriptor
import automaton.constructor.model.transition.property.TransitionPropertyGroup

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
) {
    private val properties = mutableMapOf<TransitionPropertyDescriptor<*>, TransitionProperty<*>>()
    val propertyGroups = memoryDescriptors.map { memoryUnitDescriptor ->
        TransitionPropertyGroup(
            memoryUnitDescriptor,
            memoryUnitDescriptor.filters.map { registerProperty(it) },
            memoryUnitDescriptor.sideEffects.map { registerProperty(it) }
        )
    }
    val filters: List<TransitionProperty<*>> = propertyGroups.flatMap { it.filters }
    val sideEffects: List<TransitionProperty<*>> = propertyGroups.flatMap { it.sideEffects }
    val allProperties: Collection<TransitionProperty<*>> get() = properties.values

    /**
     * Returns property of this transition that is described by the given [descriptor]
     * @see get
     * @see set
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> getProperty(descriptor: TransitionPropertyDescriptor<T>): TransitionProperty<T> =
        properties.getValue(descriptor) as TransitionProperty<T>

    /**
     * Returns value of the property of this transition that is described by the given [descriptor]
     * @see getProperty
     */
    operator fun <T> get(descriptor: TransitionPropertyDescriptor<T>): T = getProperty(descriptor).value

    /**
     * Assigns [value] to the value of the property of this transition that is described by the given [descriptor]
     * @see getProperty
     */
    operator fun <T> set(descriptor: TransitionPropertyDescriptor<T>, value: T) {
        getProperty(descriptor).value = value
    }

    /**
     * Transition is pure if all its properties have [EPSILON_VALUE] (including both filters and sideEffects)
     *
     * Pure transition is always possible regardless of memory data and has no effect on the memory data
     *
     * Executor may take pure transition without asking nor informing memory units
     */
    fun isPure() = allProperties.all { it.value == EPSILON_VALUE }

    private fun <T> registerProperty(descriptor: TransitionPropertyDescriptor<T>): TransitionProperty<T> =
        descriptor.createProperty().also { properties[descriptor] = it }
}
