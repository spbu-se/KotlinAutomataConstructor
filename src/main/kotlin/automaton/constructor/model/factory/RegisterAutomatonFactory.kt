package automaton.constructor.model.factory

import automaton.constructor.model.memory.MemoryUnitDescriptor
import automaton.constructor.model.memory.RegisterDescriptor
import automaton.constructor.model.memory.tape.InputTapeDescriptor
import automaton.constructor.utils.Setting
import javafx.scene.control.Spinner
import tornadofx.*

class RegisterAutomatonFactory : AbstractAutomatonFactory("register automaton") {
    val registerCountProperty = 1.toProperty()
    var registerCount by registerCountProperty

    override fun createMemoryDescriptors(): List<MemoryUnitDescriptor> =
        listOf(InputTapeDescriptor()) + List(registerCount) { RegisterDescriptor() }

    override fun createSettings() = listOf(
        Setting("Number of registers", Spinner<Int>(1, 5, registerCount).apply {
            registerCountProperty.bind(valueProperty())
        })
    )
}
