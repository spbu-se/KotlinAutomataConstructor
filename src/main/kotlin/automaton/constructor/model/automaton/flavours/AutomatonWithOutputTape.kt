package automaton.constructor.model.automaton.flavours

import automaton.constructor.model.memory.tape.OutputTapeDescriptor
import automaton.constructor.model.property.AutomatonElement
import automaton.constructor.model.property.DynamicProperty
import automaton.constructor.model.property.EPSILON_VALUE

interface AutomatonWithOutputTape {
    val outputTape: OutputTapeDescriptor


    val AutomatonElement.outputValueProperty: DynamicProperty<String?>
        get() = getProperty(outputTape.outputValue)

    var AutomatonElement.outputValue: String?
        get() = outputValueProperty.get()
        set(value) = outputValueProperty.set(value)

    val AutomatonElement.notNullOutputValue: String
        get() = outputValueProperty.get().takeIf { it != EPSILON_VALUE } ?: ""
}
