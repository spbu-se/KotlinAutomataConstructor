package automaton.constructor.model.automaton.flavours

import automaton.constructor.model.memory.tape.HeadMoveDirection
import automaton.constructor.model.memory.tape.MultiTrackTapeDescriptor
import automaton.constructor.model.property.DynamicProperty
import automaton.constructor.model.transition.Transition

interface AutomatonWithTapes {
    val tapes: List<MultiTrackTapeDescriptor>


    val Transition.tapesHeadMoveDirectionsProperties: List<DynamicProperty<HeadMoveDirection>>
        get() = tapes.map { getProperty(it.headMoveDirection) }

    val Transition.tapesHeadMoveDirections: DynamicPropertiesValues<HeadMoveDirection>
        get() = DynamicPropertiesValues(tapesHeadMoveDirectionsProperties)


    val Transition.tapesExpectedCharsProperties: List<DynamicProperty<Char>>
        get() = tapes.map { getProperty(it.expectedChars.first()) }

    val Transition.tapesExpectedChars: DynamicPropertiesValues<Char>
        get() = DynamicPropertiesValues(tapesExpectedCharsProperties)


    val Transition.tapesNewCharsProperties: List<DynamicProperty<Char>>
        get() = tapes.map { getProperty(it.newChars.first()) }

    val Transition.tapesNewChars: DynamicPropertiesValues<Char>
        get() = DynamicPropertiesValues(tapesNewCharsProperties)
}
