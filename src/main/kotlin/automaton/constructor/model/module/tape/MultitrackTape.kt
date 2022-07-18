package automaton.constructor.model.module.tape

import automaton.constructor.model.data.MultiTrackTapeDescriptorData
import automaton.constructor.model.memory.MemoryUnitDescriptor
import automaton.constructor.model.memory.MemoryUnitStatus
import automaton.constructor.model.memory.MemoryUnitStatus.READY_TO_ACCEPT
import automaton.constructor.model.property.DynamicPropertyDescriptors
import automaton.constructor.model.transition.Transition
import automaton.constructor.utils.monospaced
import automaton.constructor.utils.I18N.messages
import javafx.scene.layout.VBox
import tornadofx.*
import java.text.MessageFormat

class MultiTrackTapeDescriptor(val trackCount: Int) : MemoryUnitDescriptor {
    val valueProperties = List(trackCount) { "".toProperty() }
    val headMoveDirection =
        DynamicPropertyDescriptors.enum<HeadMoveDirection>(messages.getString("MultitrackTape.HeadMoveDirection"))
    val expectedChars = List(trackCount) { i ->
        DynamicPropertyDescriptors.charOrBlank(
            MessageFormat.format(
                messages.getString("MultitrackTape.ExpectedChar"),
                getIndexSuffix(i)
            )
        )
    }
    val newChars = List(trackCount) { i ->
        DynamicPropertyDescriptors.charOrBlank(
            MessageFormat.format(
                messages.getString("MultitrackTape.NewChar"),
                getIndexSuffix(i)
            )
        )
    }
    override val transitionFilters = expectedChars
    override val transitionSideEffects = newChars + headMoveDirection
    override var displayName = if (trackCount == 1) messages.getString("MultitrackTape.Tape")
    else messages.getString("MultitrackTape.Multi-trackTape")

    override fun getData() = MultiTrackTapeDescriptorData(trackCount)

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
    override val observableStatus = READY_TO_ACCEPT.toProperty()
    override val status: MemoryUnitStatus by observableStatus

    override fun takeTransition(transition: Transition) {
        tracks.forEachIndexed { i, track ->
            track.current = transition[descriptor.newChars[i]]
            track.moveHead(transition[descriptor.headMoveDirection])
        }
    }

    override fun copy() = MultiTrackTape(descriptor, tracks.map { Track(it) })
}
