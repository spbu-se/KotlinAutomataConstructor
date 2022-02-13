package automaton.constructor.model.factory

import automaton.constructor.model.memory.output.MealyOutputDescriptor
import automaton.constructor.model.memory.tape.InputTapeDescriptor

class MealyMachineFactory : AbstractAutomatonFactory("Mealy machine") {
    override fun createMemoryDescriptors() = listOf(InputTapeDescriptor(), MealyOutputDescriptor())

    override fun createSettings() = emptyList<Nothing>()
}
