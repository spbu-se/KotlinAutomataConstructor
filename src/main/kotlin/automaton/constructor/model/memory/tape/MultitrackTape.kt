package automaton.constructor.model.memory.tape

import automaton.constructor.model.memory.MemoryUnitDescriptor
import automaton.constructor.model.memory.MemoryUnitStatus.READY_TO_ACCEPT
import automaton.constructor.model.transition.Transition
import automaton.constructor.model.transition.property.createCharOrBlankTransitionPropertyDescriptor
import automaton.constructor.model.transition.property.createEnumTransitionPropertyDescriptor
import automaton.constructor.utils.monospaced
import javafx.scene.layout.VBox
import tornadofx.*

class MultiTrackTapeDescriptor(val trackCount: Int) : MemoryUnitDescriptor {
    val valueProperties = List(trackCount) { "".toProperty() }
    val headMoveDirection = createEnumTransitionPropertyDescriptor<HeadMoveDirection>("Head move")
    val expectedChars = List(trackCount) { i ->
        createCharOrBlankTransitionPropertyDescriptor("Expected char${getIndexSuffix(i)}")
    }
    val newChars = List(trackCount) { i ->
        createCharOrBlankTransitionPropertyDescriptor("New char${getIndexSuffix(i)}")
    }
    override val filters = expectedChars
    override val sideEffects = newChars + headMoveDirection
    override var displayName = if (trackCount == 1) "Tape" else "Multi-track tape"

    override fun createMemoryUnit() = MultiTrackTape(this, valueProperties.map { Track(it.value) })

    override fun createEditor() = VBox().apply {
        valueProperties.forEach { textfield(it).monospaced() }
    }

    private fun getIndexSuffix(index: Int) = if (trackCount == 1) "" else " ${index + 1}"
}

class MultiTrackTape(
    override val descriptor: MultiTrackTapeDescriptor,
    tracks: List<Track>
) : AbstractTape(tracks) {
    override val status get() = READY_TO_ACCEPT

    override fun takeTransition(transition: Transition) {
        tracks.forEachIndexed { i, track ->
            track.currentChar = transition[descriptor.newChars[i]]
            track.shiftHead(transition[descriptor.headMoveDirection].shift)
        }
    }

    override fun copy() = MultiTrackTape(descriptor, tracks.map { Track(it) })
}
