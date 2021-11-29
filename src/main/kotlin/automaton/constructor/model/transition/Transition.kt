package automaton.constructor.model.transition

import automaton.constructor.model.State
import automaton.constructor.model.memory.MemoryUnitDescriptor
import automaton.constructor.model.transition.property.TransitionProperty
import automaton.constructor.model.transition.property.TransitionPropertyDescriptor
import automaton.constructor.model.transition.property.TransitionPropertyGroup

class Transition(
    val source: State,
    val target: State,
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
    val filters: List<TransitionProperty<*>> = propertyGroups.flatMap { it.filterProperties }
    val sideEffects: List<TransitionProperty<*>> = propertyGroups.flatMap { it.sideEffectProperties }
    val allProperties: Collection<TransitionProperty<*>> get() = properties.values

    @Suppress("UNCHECKED_CAST")
    fun <T> getProperty(descriptor: TransitionPropertyDescriptor<T>): TransitionProperty<T> =
        properties[descriptor] as TransitionProperty<T>

    operator fun <T> get(descriptor: TransitionPropertyDescriptor<T>): T = getProperty(descriptor).value
    operator fun <T> set(descriptor: TransitionPropertyDescriptor<T>, value: T) {
        getProperty(descriptor).value = value
    }

    private fun <T> registerProperty(descriptor: TransitionPropertyDescriptor<T>): TransitionProperty<T> =
        descriptor.createProperty().also { properties[descriptor] = it }
}
