package automaton.constructor.model.factory

import automaton.constructor.model.memory.MemoryUnitDescriptor
import automaton.constructor.model.memory.StackDescriptor
import automaton.constructor.model.memory.tape.InputTapeDescriptor
import automaton.constructor.utils.Setting
import javafx.scene.control.Spinner
import tornadofx.*

class PushdownAutomatonFactory : AbstractAutomatonFactory("pushdown automaton") {
    val stackCountProperty = 1.toProperty()
    var stackCount by stackCountProperty

    override fun createMemoryDescriptors(): List<MemoryUnitDescriptor> =
        listOf(InputTapeDescriptor()) + List(stackCount) { StackDescriptor() }

    override fun createSettings() = listOf(
        Setting("Number of stacks", Spinner<Int>(1, 5, stackCount).apply {
            stackCountProperty.bind(valueProperty())
        })
    )
}
