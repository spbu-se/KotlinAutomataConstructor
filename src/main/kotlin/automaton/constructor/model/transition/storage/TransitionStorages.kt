package automaton.constructor.model.transition.storage

import automaton.constructor.model.memory.MemoryUnitDescriptor
import automaton.constructor.model.transition.property.TransitionPropertyDescriptor

// creates transition storage of height `memory.sumBy { it.filterDescriptors.size + it.sideEffectDescriptors.size }`
fun createTransitionStorage(memoryDescriptors: List<MemoryUnitDescriptor>) = memoryDescriptors
    .flatMap { it.filters }
    .fold<TransitionPropertyDescriptor<*>, (Int) -> TransitionStorage>({ LeafTransitionStorage() }) { acc, _ ->
        { depth -> BranchTransitionStorage(depth, acc) }
    }.invoke(0)
