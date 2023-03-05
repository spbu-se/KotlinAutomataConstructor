package automaton.constructor.model.element

import automaton.constructor.model.property.*
import javafx.beans.property.Property
import tornadofx.*

/**
 * Transition or vertex
 */
sealed class AutomatonElement(propertyDescriptorGroups: List<DynamicPropertyDescriptorGroup>) {
    private val properties = mutableMapOf<DynamicPropertyDescriptor<*>, DynamicProperty<*>>()
    val propertyGroups = propertyDescriptorGroups.map { (displayName, filters, sideEffects) ->
        DynamicPropertyGroup(
            displayName,
            filters.map { registerProperty(it) },
            sideEffects.map { registerProperty(it) }
        )
    }

    open val filters: List<DynamicProperty<*>> = propertyGroups.flatMap { it.filters }
    val sideEffects: List<DynamicProperty<*>> = propertyGroups.flatMap { it.sideEffects }
    val allProperties: Collection<DynamicProperty<*>> get() = properties.values
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

    open fun isPure() = allProperties.all { it.value == EPSILON_VALUE }

    fun readProperties(): List<String> = properties.values.map { it.toString() }
    fun writeProperties(values: List<String>) = properties.values.zip(values).forEach { (property, value) ->
        property.value = property.descriptor.stringConverter.fromString(value)
    }

    private fun <T> registerProperty(descriptor: DynamicPropertyDescriptor<T>): DynamicProperty<T> =
        descriptor.createProperty().also { properties[descriptor] = it }

    val propertiesTextBinding =
        stringBinding(
            this,
            *propertyGroups.flatMap { it.filters + it.sideEffects }.toTypedArray()
        ) {
            propertyGroups.asSequence()
                .map { (_, filters, sideEffects) ->
                    listOf(filters, sideEffects)
                        .map { dynamicProperties -> dynamicProperties.joinToString(separator = ",") { it.displayValue } }
                        .filter { it.isNotEmpty() }
                        .joinToString(separator = "/")
                }
                .filter { it.isNotEmpty() }
                .joinToString(separator = ";")
                .replace("\\n", "\n")
        }
    val propetiesText by propertiesTextBinding
}
