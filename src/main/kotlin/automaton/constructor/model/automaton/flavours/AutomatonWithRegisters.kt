package automaton.constructor.model.automaton.flavours

import automaton.constructor.model.memory.RegisterDescriptor
import automaton.constructor.model.property.DynamicProperty
import automaton.constructor.model.transition.Transition

interface AutomatonWithRegisters {
    val registers: List<RegisterDescriptor>


    val Transition.registerExpectedValuesProperties: List<DynamicProperty<String?>>
        get() = registers.map { getProperty(it.expectedValue) }

    val Transition.registerExpectedValues: DynamicPropertiesValues<String?>
        get() = DynamicPropertiesValues(registerExpectedValuesProperties)


    val Transition.registerNewValuesProperties: List<DynamicProperty<String?>>
        get() = registers.map { getProperty(it.newValue) }

    val Transition.registerNewValues: DynamicPropertiesValues<String?>
        get() = DynamicPropertiesValues(registerNewValuesProperties)
}
