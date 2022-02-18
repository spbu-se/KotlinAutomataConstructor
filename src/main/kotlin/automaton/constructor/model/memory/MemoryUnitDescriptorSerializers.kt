package automaton.constructor.model.memory

import automaton.constructor.model.memory.output.MealyOutputDescriptor
import automaton.constructor.model.memory.output.MooreOutputDescriptor
import automaton.constructor.model.memory.tape.InputTapeDescriptor
import automaton.constructor.model.memory.tape.MultiTrackTapeDescriptor
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

val memoryUnitDescriptorSerializers = SerializersModule {
    polymorphic(MemoryUnitDescriptor::class) {
        subclass(InputTapeDescriptor::class)
        subclass(MultiTrackTapeDescriptor::class)
        subclass(StackDescriptor::class)
        subclass(RegisterDescriptor::class)
        subclass(MealyOutputDescriptor::class)
        subclass(MooreOutputDescriptor::class)
    }
}
