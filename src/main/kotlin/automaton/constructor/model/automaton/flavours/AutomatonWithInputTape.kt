package automaton.constructor.model.automaton.flavours

import automaton.constructor.model.memory.tape.InputTapeDescriptor
import automaton.constructor.model.property.DynamicProperty
import automaton.constructor.model.transition.Transition

interface AutomatonWithInputTape {
    val inputTape: InputTapeDescriptor


    val Transition.inputTapeExpectedCharProperty: DynamicProperty<Char?>
        get() = getProperty(inputTape.expectedChar)

    var Transition.inputTapeExpectedChar: Char?
        get() = inputTapeExpectedCharProperty.get()
        set(value) = inputTapeExpectedCharProperty.set(value)
}
