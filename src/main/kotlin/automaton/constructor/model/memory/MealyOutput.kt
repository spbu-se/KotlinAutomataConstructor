package automaton.constructor.model.memory

import automaton.constructor.model.memory.MemoryUnitStatus.READY_TO_ACCEPT
import automaton.constructor.model.transition.Transition
import automaton.constructor.model.transition.property.EPSILON_VALUE
import automaton.constructor.model.transition.property.TransitionPropertyDescriptor
import automaton.constructor.model.transition.property.createCharOrEpsTransitionPropertyDescriptor
import automaton.constructor.utils.MonospaceEditableString
import automaton.constructor.utils.scrollToRightWhenUnfocused
import javafx.scene.Node

class MealyOutputDescriptor : MemoryUnitDescriptor {
    val outputChar = createCharOrEpsTransitionPropertyDescriptor("Output char", canBeDeemedEpsilon = false)
    override val filters = emptyList<TransitionPropertyDescriptor<*>>()
    override val sideEffects = listOf(outputChar)
    override var displayName = "Mealy output"

    override fun createMemoryUnit() = MealyOutput(this, "")

    override fun createEditor(): Node? = null
}

class MealyOutput(
    override val descriptor: MealyOutputDescriptor,
    initValue: String
) : MonospaceEditableString(initValue), MemoryUnit {
    override fun getCurrentFilterValues() = listOf<Nothing>()
    override val status get() = READY_TO_ACCEPT

    override fun takeTransition(transition: Transition) {
        val outputChar = transition[descriptor.outputChar]
        if (outputChar != EPSILON_VALUE) value += outputChar
    }

    override fun copy() = MealyOutput(descriptor, value)

    override fun createEditor() = super.createTextFieldEditor().scrollToRightWhenUnfocused()
}
