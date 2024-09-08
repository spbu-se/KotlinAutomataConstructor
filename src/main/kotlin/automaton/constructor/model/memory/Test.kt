package automaton.constructor.model.memory

import automaton.constructor.model.data.MemoryUnitDescriptorData
import kotlinx.serialization.Serializable

data class Test(val input: List<MemoryUnitDescriptor>)

@Serializable
data class TestsForSerializing(val tests: List<List<MemoryUnitDescriptorData>>, val automatonType: String)
