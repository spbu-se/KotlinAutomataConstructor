package automaton.constructor.model

import automaton.constructor.model.transition.Transition
import automaton.constructor.model.transition.property.TransitionPropertyDescriptor
import javafx.scene.Node

interface MemoryUnit {
    val filterDescriptors: List<TransitionPropertyDescriptor<*>>
    val sideEffectDescriptors: List<TransitionPropertyDescriptor<*>>

    // TODO val isAlwaysReadyToTerminate (if all memory units are always ready to terminate then there should be no transitions from final states)
    // TODO val mayRequireAcceptance (if any memory unit may require acceptance then automaton can ran without final states)
    val currentState: List<*>
    val status: Status
    var name: String
    fun takeTransition(transition: Transition)
    fun createEditor(): Node
    fun copy(): MemoryUnit

    fun canDeemTransitionEpsilon() = (filterDescriptors + sideEffectDescriptors).any { it.canBeDeemedEpsilon }

    enum class Status {
        NOT_READY_TO_TERMINATE,
        READY_TO_TERMINATE,
        REQUIRES_TERMINATION,
        REQUIRES_ACCEPTANCE
    }
}
