package automaton.constructor.model.automaton.flavours

import automaton.constructor.model.element.Transition
import automaton.constructor.model.memory.tape.InputTapeDescriptor
import automaton.constructor.model.property.DynamicProperty
import automaton.constructor.model.property.FormalRegex

interface AutomatonWithInputTape {
    val inputTape: InputTapeDescriptor

    val Transition.regexProperty: DynamicProperty<FormalRegex?>
        get() = getProperty(inputTape.expectedChar)

    var Transition.regex: FormalRegex?
        get() = regexProperty.get()
        set(value) = regexProperty.set(value)
}
