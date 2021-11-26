package automaton.constructor.model.transition.property

import automaton.constructor.model.MemoryUnit

data class TransitionPropertyGroup(
    val memoryUnit: MemoryUnit,
    val filterProperties: List<TransitionProperty<*>>,
    val sideEffectProperties: List<TransitionProperty<*>>
)
