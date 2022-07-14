package automaton.constructor.model.memory

import automaton.constructor.model.memory.output.MealyMooreOutputTapeDescriptor
import automaton.constructor.model.memory.tape.InputTapeDescriptor
import automaton.constructor.model.memory.tape.MultiTrackTapeDescriptor
import kotlinx.serialization.modules.*

val memoryUnitDescriptorSerializers = SerializersModule {
    polymorphic(MemoryUnitDescriptor::class) {
        subclass(InputTapeDescriptor::class)
        subclass(MultiTrackTapeDescriptor::class)
        subclass(StackDescriptor::class)
        subclass(RegisterDescriptor::class)
        subclass(MealyMooreOutputTapeDescriptor::class)
    }
}
