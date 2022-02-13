package automaton.constructor.model.transition.storage

import automaton.constructor.model.memory.MemoryUnitDescriptor
import automaton.constructor.model.property.DynamicPropertyDescriptor

/**
 * Creates transition storage tree of height `memory.sumBy { it.filterDescriptors.size + it.sideEffectDescriptors.size }`
 */
fun createTransitionStorageTree(memoryDescriptors: List<MemoryUnitDescriptor>) = memoryDescriptors
    .flatMap { it.transitionFilters + it.stateFilters }
    .fold<DynamicPropertyDescriptor<*>, (Int) -> TransitionStorage>({ LeafTransitionStorage() }) { acc, _ ->
        { depth -> BranchTransitionStorage(depth, acc) }
    }.invoke(0)
