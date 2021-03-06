package automaton.constructor.model.property

import javafx.beans.property.Property

/**
 * State or transition
 */
open class AutomatonElement(propertyDescriptorGroups: List<DynamicPropertyDescriptorGroup>) {
    private val properties = mutableMapOf<DynamicPropertyDescriptor<*>, DynamicProperty<*>>()
    val propertyGroups = propertyDescriptorGroups.map { (memoryDescriptor, filters, sideEffects) ->
        DynamicPropertyGroup(
            memoryDescriptor,
            filters.map { registerProperty(it) },
            sideEffects.map { registerProperty(it) }
        )
    }

    open val filters: List<DynamicProperty<*>> = propertyGroups.flatMap { it.filters }
    open val sideEffects: List<DynamicProperty<*>> = propertyGroups.flatMap { it.sideEffects }
    open val allProperties: Collection<DynamicProperty<*>> get() = properties.values
    open val undoRedoProperties: Collection<Property<*>> get() = allProperties

    /**
     * Returns property of this automaton element that is described by the given [descriptor]
     * @see get
     * @see set
     */
    @Suppress("UNCHECKED_CAST")
    open fun <T> getProperty(descriptor: DynamicPropertyDescriptor<T>): DynamicProperty<T> =
        properties.getValue(descriptor) as DynamicProperty<T>

    /**
     * Returns value of the property of this automaton element that is described by the given [descriptor]
     * @see getProperty
     */
    operator fun <T> get(descriptor: DynamicPropertyDescriptor<T>): T = getProperty(descriptor).value

    /**
     * Assigns [value] to the property of this automaton element that is described by the given [descriptor]
     * @see getProperty
     */
    operator fun <T> set(descriptor: DynamicPropertyDescriptor<T>, value: T) {
        getProperty(descriptor).value = value
    }

    fun readProperties(): List<String> = properties.values.map { it.toString() }
    fun writeProperties(values: List<String>) = properties.values.zip(values).forEach { (property, value) ->
        property.value = property.descriptor.stringConverter.fromString(value)
    }

    private fun <T> registerProperty(descriptor: DynamicPropertyDescriptor<T>): DynamicProperty<T> =
        descriptor.createProperty().also { properties[descriptor] = it }
}
