package automaton.constructor.model.transition

import automaton.constructor.model.MemoryUnit
import automaton.constructor.model.State
import automaton.constructor.model.transition.property.EPSILON_VALUE
import automaton.constructor.model.transition.property.TransitionProperty
import automaton.constructor.model.transition.property.TransitionPropertyDescriptor

class Transition(
    val source: State,
    val target: State,
    memory: List<MemoryUnit>
) {
    private val properties = mutableMapOf<TransitionPropertyDescriptor<*>, TransitionProperty<*>>()
    private val _filters = mutableListOf<TransitionProperty<*>>()
    private val _sideEffects = mutableListOf<TransitionProperty<*>>()
    val propertyGroups = memory.map { memoryUnit ->
        Triple(
            memoryUnit,
            memoryUnit.filterDescriptors.map { descriptor ->
                registerProperty(descriptor).also { property -> _filters.add(property) }
            },
            memoryUnit.sideEffectDescriptors.map { descriptor ->
                registerProperty(descriptor).also { property -> _sideEffects.add(property) }
            }
        )
    }
    val filters: List<TransitionProperty<*>> get() = _filters
    val sideEffects: List<TransitionProperty<*>> get() = _sideEffects
    val allProperties: Collection<TransitionProperty<*>> get() = properties.values

    @Suppress("UNCHECKED_CAST")
    fun <T> getProperty(descriptor: TransitionPropertyDescriptor<T>): TransitionProperty<T> =
        properties[descriptor] as TransitionProperty<T>

    operator fun <T> get(descriptor: TransitionPropertyDescriptor<T>): T = getProperty(descriptor).value
    operator fun <T> set(descriptor: TransitionPropertyDescriptor<T>, value: T) {
        getProperty(descriptor).value = value
    }

    fun isPure() = allProperties.all { it.value == EPSILON_VALUE }

    private fun <T> registerProperty(descriptor: TransitionPropertyDescriptor<T>): TransitionProperty<T> =
        descriptor.createProperty().also { properties[descriptor] = it }
}
