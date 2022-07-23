package automaton.constructor.model.memory.output

import automaton.constructor.model.memory.MemoryUnit
import automaton.constructor.model.memory.MemoryUnitDescriptor
import automaton.constructor.model.memory.MemoryUnitStatus
import automaton.constructor.model.memory.MemoryUnitStatus.READY_TO_ACCEPT
import automaton.constructor.model.property.DynamicPropertyDescriptor
import automaton.constructor.model.property.DynamicPropertyDescriptors
import automaton.constructor.model.property.EPSILON_VALUE
import automaton.constructor.model.transition.Transition
import automaton.constructor.utils.I18N.messages
import automaton.constructor.utils.MonospaceEditableString
import automaton.constructor.utils.scrollToRightWhenUnfocused
import javafx.scene.Node
import tornadofx.*

abstract class AbstractOutputTapeDescriptor : MemoryUnitDescriptor {
    override val transitionFilters get() = emptyList<DynamicPropertyDescriptor<*>>()
    override val stateFilters get() = emptyList<DynamicPropertyDescriptor<*>>()
    abstract override val transitionSideEffects: List<DynamicPropertyDescriptor<*>>
    abstract override val stateSideEffects: List<DynamicPropertyDescriptor<*>>

    val outputStringDescriptor = DynamicPropertyDescriptors.stringOrEps(
        name = messages.getString("AbstractOutputTape.OutputString"),
        canBeDeemedEpsilon = false
    ).copy(displayValueFactory = { if (it == EPSILON_VALUE) "" else it.toString() })

    abstract fun getOutput(transition: Transition): String

    override fun createMemoryUnit() = OutputTape(descriptor = this, initValue = "")

    override fun createEditor(): Node? = null
}

class OutputTape(
    override val descriptor: AbstractOutputTapeDescriptor,
    initValue: String
) : MonospaceEditableString(initValue), MemoryUnit {
    override fun getCurrentFilterValues() = listOf<Nothing>()

    override val observableStatus = READY_TO_ACCEPT.toProperty()
    override val status: MemoryUnitStatus by observableStatus

    override fun takeTransition(transition: Transition) {
        value += descriptor.getOutput(transition)
    }

    override fun copy() = OutputTape(descriptor, value)

    override fun createEditor() = super.createTextFieldEditor().scrollToRightWhenUnfocused()
}
