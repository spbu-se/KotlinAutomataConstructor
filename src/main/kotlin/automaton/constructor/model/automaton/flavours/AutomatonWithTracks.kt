package automaton.constructor.model.automaton.flavours

import automaton.constructor.model.element.Transition
import automaton.constructor.model.memory.tape.HeadMoveDirection
import automaton.constructor.model.memory.tape.MultiTrackTapeDescriptor
import automaton.constructor.model.property.DynamicProperty

interface AutomatonWithTracks {
    val tracks: MultiTrackTapeDescriptor


    val Transition.tracksHeadMoveDirectionsProperties: DynamicProperty<HeadMoveDirection>
        get() = getProperty(tracks.headMoveDirection)

    var Transition.tracksHeadMoveDirections: HeadMoveDirection
        get() = tracksHeadMoveDirectionsProperties.get()
        set(value) = tracksHeadMoveDirectionsProperties.set(value)


    val Transition.tracksExpectedCharsProperties: List<DynamicProperty<Char>>
        get() = tracks.expectedChars.map(::getProperty)

    val Transition.tracksExpectedChars: DynamicPropertiesValues<Char>
        get() = DynamicPropertiesValues(tracksExpectedCharsProperties)


    val Transition.tracksNewCharsProperties: List<DynamicProperty<Char>>
        get() = tracks.newChars.map(::getProperty)

    val Transition.tracksNewChars: DynamicPropertiesValues<Char>
        get() = DynamicPropertiesValues(tracksNewCharsProperties)
}
