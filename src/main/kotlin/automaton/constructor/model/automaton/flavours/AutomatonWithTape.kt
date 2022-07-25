package automaton.constructor.model.automaton.flavours

import automaton.constructor.model.memory.tape.HeadMoveDirection
import automaton.constructor.model.memory.tape.MultiTrackTapeDescriptor
import automaton.constructor.model.property.DynamicProperty
import automaton.constructor.model.transition.Transition

interface AutomatonWithTape {
    val tape: MultiTrackTapeDescriptor


    val Transition.tapeHeadMoveDirectionProperty: DynamicProperty<HeadMoveDirection>
        get() = getProperty(tape.headMoveDirection)

    var Transition.tapeHeadMoveDirection: HeadMoveDirection
        get() = tapeHeadMoveDirectionProperty.get()
        set(value) = tapeHeadMoveDirectionProperty.set(value)


    val Transition.tapeExpectedCharProperty: DynamicProperty<Char>
        get() = getProperty(tape.expectedChars.first())

    var Transition.tapeExpectedChar: Char
        get() = tapeExpectedCharProperty.get()
        set(value) = tapeExpectedCharProperty.set(value)


    val Transition.tapeNewCharProperty
        get() = getProperty(tape.newChars.first())

    var Transition.tapeNewChar: Char
        get() = tapeNewCharProperty.get()
        set(value) = tapeNewCharProperty.set(value)
}
