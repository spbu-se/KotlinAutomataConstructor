package automaton.constructor.model.memory.output

import automaton.constructor.model.memory.*
import automaton.constructor.model.memory.MemoryUnitStatus.READY_TO_ACCEPT
import automaton.constructor.model.property.*
import automaton.constructor.model.transition.Transition
import automaton.constructor.utils.MonospaceEditableString
import automaton.constructor.utils.scrollToRightWhenUnfocused
import automaton.constructor.utils.I18N.messages
import javafx.scene.Node
import tornadofx.*

abstract class AbstractOutputTapeDescriptor : MemoryUnitDescriptor {
    override val transitionFilters get() = emptyList<DynamicPropertyDescriptor<*>>()
    override val stateFilters get() = emptyList<DynamicPropertyDescriptor<*>>()
    abstract override val transitionSideEffects: List<DynamicPropertyDescriptor<*>>
    abstract override val stateSideEffects: List<DynamicPropertyDescriptor<*>>

    val outputCharDescriptor = DynamicPropertyDescriptors.charOrEps(
        name = messages.getString("AbstractOutputTape.OutputChar"),
        canBeDeemedEpsilon = false
    ).copy(displayValueFactory = { if (it == EPSILON_VALUE) "" else it.toString() })

    abstract fun getOutput(transition: Transition): List<Char?>

    override fun createMemoryUnit() = OutputTape(this, "")

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
            .filter { it != EPSILON_VALUE }
            .joinToString(separator = "")
    }

    override fun copy() = OutputTape(descriptor, value)

    override fun createEditor() = super.createTextFieldEditor().scrollToRightWhenUnfocused()
}
