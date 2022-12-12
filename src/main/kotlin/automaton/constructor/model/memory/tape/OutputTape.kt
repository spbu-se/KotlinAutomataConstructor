package automaton.constructor.model.memory.tape

import automaton.constructor.model.data.OutputTapeDescriptorData
import automaton.constructor.model.element.State
import automaton.constructor.model.element.Transition
import automaton.constructor.model.memory.AcceptanceRequiringPolicy
import automaton.constructor.model.memory.MemoryUnit
import automaton.constructor.model.memory.MemoryUnitDescriptor
import automaton.constructor.model.memory.MemoryUnitStatus
import automaton.constructor.model.memory.MemoryUnitStatus.REQUIRES_ACCEPTANCE
import automaton.constructor.model.property.DynamicPropertyDescriptor
import automaton.constructor.model.property.DynamicPropertyDescriptors
import automaton.constructor.model.property.EPSILON_VALUE
import automaton.constructor.utils.I18N
import automaton.constructor.utils.MonospaceEditableString
import automaton.constructor.utils.scrollToRightWhenUnfocused
import javafx.scene.Node
import tornadofx.*

class OutputTapeDescriptor : MemoryUnitDescriptor {
    val outputValue = DynamicPropertyDescriptors.stringOrEps(
        name = I18N.messages.getString("AbstractOutputTape.OutputString"),
        canBeDeemedEpsilon = false
    ).copy(displayValueFactory = { if (it == EPSILON_VALUE) "" else it.toString() })

    override val transitionFilters get() = emptyList<DynamicPropertyDescriptor<*>>()
    override val transitionSideEffects = listOf(outputValue)
    override val stateSideEffects = listOf(outputValue)

    override val acceptanceRequiringPolicy get() = AcceptanceRequiringPolicy.ALWAYS

    override fun createMemoryUnit() = OutputTape(descriptor = this, initValue = "")

    override fun createEditor(): Node? = null

    override var displayName: String = I18N.messages.getString("OutputTape")

    override fun getData() = OutputTapeDescriptorData
}

class OutputTape(
    override val descriptor: OutputTapeDescriptor,
    initValue: String
) : MonospaceEditableString(initValue), MemoryUnit {
    override fun getCurrentFilterValues() = listOf<Nothing>()

    override val observableStatus = REQUIRES_ACCEPTANCE.toProperty()
    override val status: MemoryUnitStatus by observableStatus

    override fun onTransition(transition: Transition) {
        value += transition[descriptor.outputValue].takeIf { it != EPSILON_VALUE } ?: ""
    }

    override fun onStateEntered(state: State) {
        value += state[descriptor.outputValue].takeIf { it != EPSILON_VALUE } ?: ""
    }

    override fun copy() = OutputTape(descriptor, value)

    override fun createEditor() = super.createTextFieldEditor().scrollToRightWhenUnfocused()
}
